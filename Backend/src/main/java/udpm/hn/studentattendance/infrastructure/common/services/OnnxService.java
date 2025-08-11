package udpm.hn.studentattendance.infrastructure.common.services;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Batchifier;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Service
public class OnnxService {

    private static final int SIZE_ANTISPOOF = 224;
    private static final int SIZE_ARCFACE = 112;
    private static final int SIZE_DEPTH = 256;

    @Value("${app.config.path.model}")
    private String modelPath;

    private ZooModel<byte[], float[]> antiSpoofModel;
    private ZooModel<byte[], float[]> arcFaceModel;
    private ZooModel<byte[], float[]> depthModel;

    private BlockingQueue<Predictor<byte[], float[]>> antiSpoofPredictorPool;
    private BlockingQueue<Predictor<byte[], float[]>> arcFacePredictorPool;
    private BlockingQueue<Predictor<byte[], float[]>> depthPredictorPool;

    private final int POOL_SIZE = Runtime.getRuntime().availableProcessors();

    @PostConstruct
    public void init() throws IOException, ModelNotFoundException, MalformedModelException {
        antiSpoofModel = ModelZoo.loadModel(buildAntiSpoofCriteria());
        arcFaceModel = ModelZoo.loadModel(buildArcFaceCriteria());
        depthModel = ModelZoo.loadModel(buildDepthCriteria());

        antiSpoofPredictorPool = new ArrayBlockingQueue<>(POOL_SIZE);
        arcFacePredictorPool = new ArrayBlockingQueue<>(POOL_SIZE);
        depthPredictorPool = new ArrayBlockingQueue<>(POOL_SIZE);

        for (int i = 0; i < POOL_SIZE; i++) {
            antiSpoofPredictorPool.add(antiSpoofModel.newPredictor());
            arcFacePredictorPool.add(arcFaceModel.newPredictor());
            depthPredictorPool.add(depthModel.newPredictor());
        }
    }

    @PreDestroy
    public void close() {
        antiSpoofPredictorPool.forEach(Predictor::close);
        arcFacePredictorPool.forEach(Predictor::close);
        depthPredictorPool.forEach(Predictor::close);
        antiSpoofModel.close();
        arcFaceModel.close();
        depthModel.close();
    }

    private Criteria<byte[], float[]> buildAntiSpoofCriteria() {
        Translator<byte[], float[]> translator = new Translator<>() {
            @Override
            public NDList processInput(TranslatorContext ctx, byte[] input) throws IOException {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(input));
                BufferedImage resized = resizeImage(img, SIZE_ANTISPOOF);
                float[] data = bufferedImageToCHWFloatArray(resized);
                return new NDList(ctx.getNDManager().create(data, new Shape(1, 3, SIZE_ANTISPOOF, SIZE_ANTISPOOF)));
            }
            @Override
            public float[] processOutput(TranslatorContext ctx, NDList list) {
                return list.singletonOrThrow().toFloatArray();
            }
            @Override
            public Batchifier getBatchifier() { return null; }
        };

        return Criteria.builder()
                .setTypes(byte[].class, float[].class)
                .optModelPath(Paths.get(modelPath, "antiSpoof.onnx").toAbsolutePath())
                .optEngine("OnnxRuntime")
                .optOption("executionProvider", "CPUExecutionProvider")
                .optOption("device", "cpu")
                .optTranslator(translator)
                .build();
    }

    private Criteria<byte[], float[]> buildArcFaceCriteria() {
        Translator<byte[], float[]> translator = new Translator<>() {
            @Override
            public NDList processInput(TranslatorContext ctx, byte[] input) throws IOException {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(input));
                BufferedImage resized = resizeImage(img, SIZE_ARCFACE);
                float[] data = bufferedImageToCHWFloatArray(resized, true);
                return new NDList(ctx.getNDManager().create(data, new Shape(1, 3, SIZE_ARCFACE, SIZE_ARCFACE)));
            }
            @Override
            public float[] processOutput(TranslatorContext ctx, NDList list) {
                return list.singletonOrThrow().toFloatArray();
            }
            @Override
            public Batchifier getBatchifier() { return null; }
        };

        return Criteria.builder()
                .setTypes(byte[].class, float[].class)
                .optModelPath(Paths.get(modelPath, "embedding.onnx").toAbsolutePath())
                .optEngine("OnnxRuntime")
                .optOption("executionProvider", "CPUExecutionProvider")
                .optOption("device", "cpu")
                .optTranslator(translator)
                .build();
    }

    private Criteria<byte[], float[]> buildDepthCriteria() {
        Translator<byte[], float[]> translator = new Translator<>() {
            @Override
            public NDList processInput(TranslatorContext ctx, byte[] input) throws IOException {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(input));
                BufferedImage resized = resizeImage(img, SIZE_DEPTH);
                float[] data = bufferedImageToCHWFloatArray(resized);
                return new NDList(ctx.getNDManager().create(data, new Shape(1, 3, SIZE_DEPTH, SIZE_DEPTH)));
            }
            @Override
            public float[] processOutput(TranslatorContext ctx, NDList list) {
                NDArray arr = list.singletonOrThrow();
                long[] shape = arr.getShape().getShape();
                float[] flat = arr.toFloatArray();

                int H = 0, W = 0;
                if (shape.length == 4) {
                    H = (int) shape[2];
                    W = (int) shape[3];
                } else if (shape.length == 3) {
                    H = (int) shape[1];
                    W = (int) shape[2];
                } else if (shape.length == 2) {
                    H = (int) shape[0];
                    W = (int) shape[1];
                } else if (shape.length == 1) {
                    W = SIZE_DEPTH;
                    H = flat.length / W;
                    if (H * W != flat.length) {
                        throw new IllegalStateException("Unexpected flat length: " + flat.length);
                    }
                } else {
                    throw new IllegalStateException("Unexpected NDArray shape: " + Arrays.toString(shape));
                }

                float minPred = Float.MAX_VALUE;
                float maxPred = Float.MIN_VALUE;
                for (float v : flat) {
                    if (v < minPred) minPred = v;
                    if (v > maxPred) maxPred = v;
                }
                float origRange = maxPred - minPred;

                float rangeNorm = maxPred - minPred;
                if (rangeNorm != 0) {
                    for (int i = 0; i < flat.length; i++) {
                        flat[i] = (flat[i] - minPred) / rangeNorm;
                    }
                }

                double sum = 0, sumSq = 0, gradSum = 0;
                float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
                for (int y = 0; y < H; y++) {
                    for (int x = 0; x < W; x++) {
                        float v = flat[y * W + x];
                        sum += v;
                        sumSq += v * v;
                        if (v < min) min = v;
                        if (v > max) max = v;
                        if (x < W - 1) gradSum += Math.abs(v - flat[y * W + (x + 1)]);
                        if (y < H - 1) gradSum += Math.abs(v - flat[(y + 1) * W + x]);
                    }
                }

                double mean = sum / (H * W);
                float variance = (float) ((sumSq / (H * W)) - (mean * mean));
                float gradScore = (float) (gradSum / ((W - 1) * H + (H - 1) * W));

                return new float[] {variance, origRange, gradScore};
            }
            @Override
            public Batchifier getBatchifier() { return null; }
        };

        return Criteria.builder()
                .setTypes(byte[].class, float[].class)
                .optModelPath(Paths.get(modelPath, "midas.onnx").toAbsolutePath())
                .optEngine("OnnxRuntime")
                .optOption("executionProvider", "CPUExecutionProvider")
                .optOption("device", "cpu")
                .optTranslator(translator)
                .build();
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int size) {
        BufferedImage resized = new BufferedImage(size, size, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, size, size, null);
        g2d.dispose();
        return resized;
    }

    private float[] bufferedImageToCHWFloatArray(BufferedImage image) {
        int w = image.getWidth(), h = image.getHeight();
        float[] mean = {0.485f, 0.456f, 0.406f};
        float[] std = {0.229f, 0.224f, 0.225f};
        float[] data = new float[3 * w * h];
        for (int c = 0; c < 3; c++) {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgb = image.getRGB(x, y);
                    int value = (c == 0) ? (rgb >> 16) & 0xFF :
                            (c == 1) ? (rgb >> 8) & 0xFF :
                                    rgb & 0xFF;
                    data[c * w * h + y * w + x] = (value / 255f - mean[c]) / std[c];
                }
            }
        }
        return data;
    }

    private float[] bufferedImageToCHWFloatArray(BufferedImage image, boolean normalizeArcface) {
        int w = image.getWidth(), h = image.getHeight();
        float[] data = new float[3 * w * h];
        for (int c = 0; c < 3; c++) {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgb = image.getRGB(x, y);
                    int value = (c == 0) ? (rgb >> 16) & 0xFF :
                            (c == 1) ? (rgb >> 8) & 0xFF :
                                    rgb & 0xFF;
                    data[c * w * h + y * w + x] = normalizeArcface ?
                            (value - 127.5f) / 128f :
                            value / 255f;
                }
            }
        }
        return data;
    }

    public static float[] normalize(float[] vec) {
        double norm = 0;
        for (float v : vec) norm += v * v;
        norm = Math.sqrt(norm);
        float[] out = new float[vec.length];
        for (int i = 0; i < vec.length; i++) out[i] = (float) (vec[i] / norm);
        return out;
    }

    public float antiSpoof(byte[] imgBytes) throws InterruptedException, TranslateException {
        Predictor<byte[], float[]> predictor = antiSpoofPredictorPool.take();
        try {
            float[] result = predictor.predict(imgBytes);
            return result[0];
        } finally {
            antiSpoofPredictorPool.put(predictor);
        }
    }

    public float[] getEmbedding(byte[] imgBytes) throws InterruptedException, TranslateException {
        Predictor<byte[], float[]> predictor = arcFacePredictorPool.take();
        try {
            return normalize(predictor.predict(imgBytes));
        } finally {
            arcFacePredictorPool.put(predictor);
        }
    }

    public boolean isDepthReal(byte[] imgBytes) throws InterruptedException, TranslateException {
        Predictor<byte[], float[]> predictor = depthPredictorPool.take();
        try {
            float[] result = predictor.predict(imgBytes);
            return result[0] > 0.06 && result[1] > 800 && result[2] > 0.0035;
        } finally {
            depthPredictorPool.put(predictor);
        }
    }

}