package udpm.hn.studentattendance.infrastructure.common.services;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.DataType;
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
import udpm.hn.studentattendance.helpers.RouterHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Service
public class OnnxService {

    private static final int SIZE_ANTISPOOF = 224;
    private static final int SIZE_ANTISPOOF2 = 112;
    private static final int SIZE_ANTISPOOF3 = 256;
    private static final int SIZE_ANTISPOOF4 = 224;
    private static final int SIZE_ANTISPOOF5 = 224;
    private static final int SIZE_ARCFACE = 112;

    @Value("${app.config.path.model}")
    private String modelPath;

    private ZooModel<byte[], float[]> antiSpoofModel;
    private ZooModel<byte[], float[]> antiSpoof2Model;
    private ZooModel<byte[], float[]> antiSpoof3Model;
    private ZooModel<byte[], float[]> antiSpoof4Model;
    private ZooModel<byte[], float[]> antiSpoof5Model;
    private ZooModel<byte[], float[]> arcFaceModel;

    private BlockingQueue<Predictor<byte[], float[]>> antiSpoofPredictorPool;
    private BlockingQueue<Predictor<byte[], float[]>> antiSpoof2PredictorPool;
    private BlockingQueue<Predictor<byte[], float[]>> antiSpoof3PredictorPool;
    private BlockingQueue<Predictor<byte[], float[]>> antiSpoof4PredictorPool;
    private BlockingQueue<Predictor<byte[], float[]>> antiSpoof5PredictorPool;
    private BlockingQueue<Predictor<byte[], float[]>> arcFacePredictorPool;

    private final int POOL_SIZE = Runtime.getRuntime().availableProcessors();

    @PostConstruct
    public void init() throws IOException, ModelNotFoundException, MalformedModelException {
        antiSpoofModel = ModelZoo.loadModel(buildAntiSpoofCriteria());
        antiSpoof2Model = ModelZoo.loadModel(buildAntiSpoof2Criteria());
        antiSpoof3Model = ModelZoo.loadModel(buildAntiSpoof3Criteria());
        antiSpoof4Model = ModelZoo.loadModel(buildAntiSpoof4Criteria());
        antiSpoof5Model = ModelZoo.loadModel(buildAntiSpoof5Criteria());
        arcFaceModel = ModelZoo.loadModel(buildArcFaceCriteria());

        antiSpoofPredictorPool = new ArrayBlockingQueue<>(POOL_SIZE);
        antiSpoof2PredictorPool = new ArrayBlockingQueue<>(POOL_SIZE);
        antiSpoof3PredictorPool = new ArrayBlockingQueue<>(POOL_SIZE);
        antiSpoof4PredictorPool = new ArrayBlockingQueue<>(POOL_SIZE);
        antiSpoof5PredictorPool = new ArrayBlockingQueue<>(POOL_SIZE);
        arcFacePredictorPool = new ArrayBlockingQueue<>(POOL_SIZE);

        for (int i = 0; i < POOL_SIZE; i++) {
            antiSpoofPredictorPool.add(antiSpoofModel.newPredictor());
            antiSpoof2PredictorPool.add(antiSpoof2Model.newPredictor());
            antiSpoof3PredictorPool.add(antiSpoof3Model.newPredictor());
            antiSpoof4PredictorPool.add(antiSpoof4Model.newPredictor());
            antiSpoof5PredictorPool.add(antiSpoof5Model.newPredictor());
            arcFacePredictorPool.add(arcFaceModel.newPredictor());
        }
    }

    @PreDestroy
    public void close() {
        antiSpoofPredictorPool.forEach(Predictor::close);
        antiSpoof2PredictorPool.forEach(Predictor::close);
        antiSpoof3PredictorPool.forEach(Predictor::close);
        antiSpoof4PredictorPool.forEach(Predictor::close);
        antiSpoof5PredictorPool.forEach(Predictor::close);
        arcFacePredictorPool.forEach(Predictor::close);
        antiSpoofModel.close();
        antiSpoof2Model.close();
        antiSpoof3Model.close();
        antiSpoof4Model.close();
        antiSpoof5Model.close();
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

    private Criteria<byte[], float[]> buildAntiSpoof2Criteria() {
        Translator<byte[], float[]> translator = new Translator<>() {
            @Override
            public NDList processInput(TranslatorContext ctx, byte[] input) throws IOException {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(input));
                BufferedImage resized = resizeImage(img, SIZE_ANTISPOOF2);
                float[] data = bufferedImageToCHWFloatArray(resized);
                return new NDList(ctx.getNDManager().create(data, new Shape(1, 3, SIZE_ANTISPOOF2, SIZE_ANTISPOOF2)));
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
                .optModelPath(Paths.get(modelPath, "modelrgb.onnx").toAbsolutePath())
                .optEngine("OnnxRuntime")
                .optOption("executionProvider", "CPUExecutionProvider")
                .optOption("device", "cpu")
                .optTranslator(translator)
                .build();
    }

    private Criteria<byte[], float[]> buildAntiSpoof3Criteria() {
        Translator<byte[], float[]> translator = new Translator<>() {
            @Override
            public NDList processInput(TranslatorContext ctx, byte[] input) throws IOException {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(input));
                BufferedImage resized = resizeImage(img, SIZE_ANTISPOOF3);
                float[] data = bufferedImageToCHWFloatArray(resized);
                return new NDList(ctx.getNDManager().create(data, new Shape(1, 3, SIZE_ANTISPOOF3, SIZE_ANTISPOOF3)));
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
                .optModelPath(Paths.get(modelPath, "m8.onnx").toAbsolutePath())
                .optEngine("OnnxRuntime")
                .optOption("executionProvider", "CPUExecutionProvider")
                .optOption("device", "cpu")
                .optTranslator(translator)
                .build();
    }

    private Criteria<byte[], float[]> buildAntiSpoof4Criteria() {
        Translator<byte[], float[]> translator = new Translator<>() {
            @Override
            public NDList processInput(TranslatorContext ctx, byte[] input) throws IOException {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(input));
                BufferedImage resized = resizeImage(img, SIZE_ANTISPOOF4);
                float[] data = bufferedImageToCHWFloatArray(resized);
                return new NDList(ctx.getNDManager().create(data, new Shape(1, 3, SIZE_ANTISPOOF4, SIZE_ANTISPOOF4)));
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
                .optModelPath(Paths.get(modelPath, "model_quantized.onnx").toAbsolutePath())
                .optEngine("OnnxRuntime")
                .optOption("executionProvider", "CPUExecutionProvider")
                .optOption("device", "cpu")
                .optTranslator(translator)
                .build();
    }

    private Criteria<byte[], float[]> buildAntiSpoof5Criteria() {
        Translator<byte[], float[]> translator = new Translator<>() {
            @Override
            public NDList processInput(TranslatorContext ctx, byte[] input) throws IOException {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(input));
                BufferedImage resized = resizeImage(img, SIZE_ANTISPOOF5);
                float[] data = bufferedImageToCHWFloatArray(resized);
                return new NDList(ctx.getNDManager().create(data, new Shape(1, 3, SIZE_ANTISPOOF5, SIZE_ANTISPOOF5)));
            }
            @Override
            public float[] processOutput(TranslatorContext ctx, NDList list) {
                return list.get(1).toFloatArray();
            }
            @Override
            public Batchifier getBatchifier() { return null; }
        };

        return Criteria.builder()
                .setTypes(byte[].class, float[].class)
                .optModelPath(Paths.get(modelPath, "OULU_Protocol_2_model_0_0.onnx").toAbsolutePath())
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

    public static boolean isColorFake(byte[] imgBytes) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imgBytes)) {
            BufferedImage image = ImageIO.read(bais);
            image = cropWithPadding(image, 350);

            long sumR = 0, sumG = 0, sumB = 0;
            int count = 0;

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    Color c = new Color(image.getRGB(x, y), true);
                    sumR += c.getRed();
                    sumG += c.getGreen();
                    sumB += c.getBlue();
                    count++;
                }
            }

            if (count == 0) return true;

            int avgR = (int) (sumR / count);
            int avgG = (int) (sumG / count);
            int avgB = (int) (sumB / count);

            float[] hsv = Color.RGBtoHSB(avgR, avgG, avgB, null);
            float hue = hsv[0] * 360;
            float sat = hsv[1];
            float brightness = hsv[2];

            if (sat > 0.1 && hue >= 120 && hue <= 240) return true;
            if (sat <= 0.2 && brightness >= 0.85) return true;

            return false;
        }
    }

    public static BufferedImage cropWithPadding(BufferedImage src, int padding) {
        if (padding < 1) {
            return src;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        int side = Math.min(w, h) - 2 * padding;
        if (side <= 0) {
            return src;
        }
        int x = (w - side) / 2;
        int y = (h - side) / 2;
        BufferedImage dest = new BufferedImage(side, side, src.getType());
        Graphics g = dest.getGraphics();
        g.drawImage(src, 0, 0, side, side, x, y, x + side, y + side, null);
        g.dispose();
        return dest;
    }

    private static float[] softmax(float[] logits) {
        float max = Float.NEGATIVE_INFINITY;
        for (float l : logits) if (l > max) max = l;

        float sum = 0f;
        float[] exps = new float[logits.length];
        for (int i = 0; i < logits.length; i++) {
            exps[i] = (float)Math.exp(logits[i] - max);
            sum += exps[i];
        }
        for (int i = 0; i < exps.length; i++) exps[i] /= sum;
        return exps;
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

    public float antiSpoof2(byte[] imgBytes) throws InterruptedException, TranslateException {
        Predictor<byte[], float[]> predictor = antiSpoof2PredictorPool.take();
        try {
            float[] result = predictor.predict(imgBytes);
            float[] softmax = softmax(result);
            return softmax[0];
        } finally {
            antiSpoof2PredictorPool.put(predictor);
        }
    }

    public float antiSpoof3(byte[] imgBytes) throws InterruptedException, TranslateException {
        Predictor<byte[], float[]> predictor = antiSpoof3PredictorPool.take();
        try {
            float[] result = predictor.predict(imgBytes);
            float[] softmax = softmax(result);
            return softmax[0];
        } finally {
            antiSpoof3PredictorPool.put(predictor);
        }
    }

    public float antiSpoof4(byte[] imgBytes) throws InterruptedException, TranslateException {
        Predictor<byte[], float[]> predictor = antiSpoof4PredictorPool.take();
        try {
            float[] result = predictor.predict(imgBytes);
            float[] softmax = softmax(result);
            return softmax[1];
        } finally {
            antiSpoof4PredictorPool.put(predictor);
        }
    }

    public float antiSpoof5(byte[] imgBytes) throws InterruptedException, TranslateException {
        Predictor<byte[], float[]> predictor = antiSpoof5PredictorPool.take();
        try {
            float[] result = predictor.predict(imgBytes);
            return result[0];
        } finally {
            antiSpoof5PredictorPool.put(predictor);
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

    public boolean isFake(byte[] faceBox, byte[] canvas)  {
        try {
            if (isColorFake(faceBox)) {
                System.out.println("isColorFake");
//                return true;
            }

            float antiSpoof = antiSpoof(faceBox);
            float antiSpoof2 = antiSpoof2(faceBox);
            float antiSpoof3 = antiSpoof3(canvas);
            float antiSpoof4 = antiSpoof4(faceBox);
            float antiSpoof5 = antiSpoof5(faceBox);

            List<Boolean> checking = new ArrayList<>();
            checking.add(antiSpoof < 0.01);
            checking.add(antiSpoof2 < 0.65);
            checking.add(antiSpoof3 < 0.6);
            checking.add(antiSpoof4 < 0.6);
            checking.add(antiSpoof5 < 0.6);

            System.out.println("antiSpoof: " + antiSpoof);
            System.out.println("antiSpoof2: " + antiSpoof2);
            System.out.println("antiSpoof3: " + antiSpoof3);
            System.out.println("antiSpoof4: " + antiSpoof4);
            System.out.println("antiSpoof5: " + antiSpoof5);

            System.out.println(checking);
            long totalReject = checking.stream().filter(Boolean::booleanValue).count();

            if (antiSpoof > 0.99 && totalReject < 3) {
                return false;
            }
            return totalReject > 2;
        } catch (Exception e) {
            return true;
        }
    }

}