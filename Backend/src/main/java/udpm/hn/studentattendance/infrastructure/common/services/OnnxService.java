package udpm.hn.studentattendance.infrastructure.common.services;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Service
public class OnnxService {

    private static final int SIZE_ANTISPOOF = 224;
    private static final int SIZE_ARCFACE = 112;

    @Value("${app.config.path.model}")
    private String modelPath;

    private ZooModel<byte[], float[]> antiSpoofModel;
    private ZooModel<byte[], float[]> arcFaceModel;

    private BlockingQueue<Predictor<byte[], float[]>> antiSpoofPredictorPool;
    private BlockingQueue<Predictor<byte[], float[]>> arcFacePredictorPool;

    private final int POOL_SIZE = Runtime.getRuntime().availableProcessors();

    @PostConstruct
    public void init() throws IOException, ModelNotFoundException, MalformedModelException {
        antiSpoofModel = ModelZoo.loadModel(buildAntiSpoofCriteria());
        arcFaceModel = ModelZoo.loadModel(buildArcFaceCriteria());

        antiSpoofPredictorPool = new ArrayBlockingQueue<>(POOL_SIZE);
        arcFacePredictorPool = new ArrayBlockingQueue<>(POOL_SIZE);

        for (int i = 0; i < POOL_SIZE; i++) {
            antiSpoofPredictorPool.add(antiSpoofModel.newPredictor());
            arcFacePredictorPool.add(arcFaceModel.newPredictor());
        }
    }

    @PreDestroy
    public void close() {
        antiSpoofPredictorPool.forEach(Predictor::close);
        arcFacePredictorPool.forEach(Predictor::close);
        antiSpoofModel.close();
        arcFaceModel.close();
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
}