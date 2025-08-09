package udpm.hn.studentattendance.infrastructure.common.services;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Batchifier;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import ai.djl.ndarray.types.Shape;

@Service
public class OnnxService {

    private final ZooModel<byte[], float[]> modelAntiSpoof;
    private final Predictor<byte[], float[]> predictorAntiSpoof;

    private final ZooModel<byte[], float[]> modelArcFace;
    private final Predictor<byte[], float[]> predictorArcFace;

    private final static int SIZE_ANTISPOOF = 224;
    private static final int SIZE_ARCFACE = 112;

    public OnnxService() throws IOException, ModelNotFoundException, MalformedModelException {
        Translator<byte[], float[]> translatorAntiSpoof = new Translator<>() {
            @Override
            public NDList processInput(TranslatorContext ctx, byte[] input) throws IOException {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(input));
                BufferedImage resized = resizeImage(img, SIZE_ANTISPOOF);

                float[] data = bufferedImageToCHWFloatArray(resized);
                NDManager manager = ctx.getNDManager();
                return new NDList(manager.create(data, new Shape(1, 3, SIZE_ANTISPOOF, SIZE_ANTISPOOF)));
            }

            @Override
            public float[] processOutput(TranslatorContext ctx, NDList list) {
                return list.singletonOrThrow().toFloatArray();
            }

            @Override
            public Batchifier getBatchifier() {
                return null;
            }
        };

        Translator<byte[], float[]> translatorArcFace = new Translator<>() {
            @Override
            public NDList processInput(TranslatorContext ctx, byte[] input) throws IOException {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(input));
                BufferedImage resized = resizeImage(img, SIZE_ARCFACE);
                float[] data = bufferedImageToCHWFloatArray(resized, true);
                NDManager manager = ctx.getNDManager();
                return new NDList(manager.create(data, new Shape(1, 3, SIZE_ARCFACE, SIZE_ARCFACE)));
            }

            @Override
            public float[] processOutput(TranslatorContext ctx, NDList list) {
                return list.singletonOrThrow().toFloatArray();
            }

            @Override
            public Batchifier getBatchifier() {
                return null;
            }
        };

        Criteria<byte[], float[]> criteria = Criteria.builder()
                .setTypes(byte[].class, float[].class)
                .optModelPath(Paths.get("src/main/resources/model/antiSpoof.onnx"))
                .optEngine("OnnxRuntime")
                .optTranslator(translatorAntiSpoof)
                .build();

        modelAntiSpoof = ModelZoo.loadModel(criteria);
        predictorAntiSpoof = modelAntiSpoof.newPredictor();

        Criteria<byte[], float[]> criteriaArcFace = Criteria.builder()
                .setTypes(byte[].class, float[].class)
                .optModelPath(Paths.get("src/main/resources/model/embedding.onnx"))
                .optEngine("OnnxRuntime")
                .optTranslator(translatorArcFace)
                .build();

        modelArcFace = ModelZoo.loadModel(criteriaArcFace);
        predictorArcFace = modelArcFace.newPredictor();
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int size) {
        BufferedImage resized = new BufferedImage(size, size, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics2D = resized.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(originalImage, 0, 0, size, size, null);
        graphics2D.dispose();
        return resized;
    }

    private float[] bufferedImageToCHWFloatArray(BufferedImage image, boolean normalizeArcface) {
        int width = image.getWidth();
        int height = image.getHeight();
        float[] data = new float[3 * width * height];
        for (int c = 0; c < 3; c++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    int value = (c == 0) ? (rgb >> 16) & 0xFF :
                            (c == 1) ? (rgb >> 8) & 0xFF :
                                    rgb & 0xFF;
                    if (normalizeArcface) {
                        data[c * width * height + y * width + x] = (value - 127.5f) / 128f;
                    } else {
                        data[c * width * height + y * width + x] = value / 255f;
                    }
                }
            }
        }
        return data;
    }

    private float[] bufferedImageToCHWFloatArray(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        float[] mean = {0.485f, 0.456f, 0.406f};
        float[] std = {0.229f, 0.224f, 0.225f};
        float[] data = new float[3 * width * height];

        for (int c = 0; c < 3; c++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    int value;
                    if (c == 0) {
                        value = (rgb >> 16) & 0xFF;
                    } else if (c == 1) {
                        value = (rgb >> 8) & 0xFF;
                    } else {
                        value = rgb & 0xFF;
                    }
                    float normalized = (value / 255f - mean[c]) / std[c];
                    data[c * width * height + y * width + x] = normalized;
                }
            }
        }
        return data;
    }

    public static float[] normalize(float[] vec) {
        double norm = 0;
        for (float v : vec) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        float[] out = new float[vec.length];
        for (int i = 0; i < vec.length; i++) {
            out[i] = (float) (vec[i] / norm);
        }
        return out;
    }

    public float antiSpoof(byte[] imgBytes) throws TranslateException {
        float[] result = predictorAntiSpoof.predict(imgBytes);
        return result[0];
    }

    public float[] getEmbedding(byte[] imgBytes) throws TranslateException {
        return normalize(predictorArcFace.predict(imgBytes));
    }

    @PreDestroy
    public void close() {
        predictorAntiSpoof.close();
        modelAntiSpoof.close();

        predictorArcFace.close();
        modelArcFace.close();
    }

}