package io.github.jacidprogrammer.verifactu.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Renders the invoice QR code as the AEAT requires (QR spec v0.5.0, section 3 and Orden HAC/1177/2024 art. 21):
 * ISO/IEC 18004, error correction level M, a quiet zone of at least 2 mm (4 modules here, the ISO default).
 *
 * <p>Printed size (30 to 40 mm) and the captions "QR tributario:" above and "VERI*FACTU" or
 * "Factura verificable en la sede electrónica de la AEAT" below are the invoice layout's responsibility.
 */
public final class QrCode {

    /** Quiet-zone width in modules. ISO/IEC 18004 asks for 4; the AEAT asks for at least 2 mm. */
    public static final int QUIET_ZONE_MODULES = 4;

    private final BitMatrix matrix;

    private QrCode(BitMatrix matrix) {
        this.matrix = matrix;
    }

    /**
     * Encodes the URL into a QR of roughly {@code sizePx} pixels per side (ZXing rounds to whole modules).
     */
    public static QrCode of(VerificationUrl url, int sizePx) {
        if (sizePx < 21) {
            throw new IllegalArgumentException("sizePx must be at least 21, got " + sizePx);
        }
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, QUIET_ZONE_MODULES);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        try {
            return new QrCode(new QRCodeWriter().encode(url.toString(), BarcodeFormat.QR_CODE, sizePx, sizePx, hints));
        } catch (WriterException e) {
            throw new IllegalArgumentException("URL cannot be encoded as a QR code: " + url, e);
        }
    }

    public int width() {
        return matrix.getWidth();
    }

    public int height() {
        return matrix.getHeight();
    }

    /** True for a dark module (black), false for a light one. */
    public boolean isDark(int x, int y) {
        return matrix.get(x, y);
    }

    public BufferedImage toImage() {
        BufferedImage image = new BufferedImage(width(), height(), BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < height(); y++) {
            for (int x = 0; x < width(); x++) {
                image.setRGB(x, y, isDark(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }
        return image;
    }

    public byte[] toPng() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (!ImageIO.write(toImage(), "png", out)) {
                throw new IllegalStateException("no PNG writer available in this JVM");
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
