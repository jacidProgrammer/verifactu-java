package io.github.jacidprogrammer.verifactu.qr;

import io.github.jacidprogrammer.verifactu.AeatEnvironment;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import io.github.jacidprogrammer.verifactu.model.InvoiceId;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class QrCodeTest {

    static final VerificationUrl URL = VerificationUrl.verifiable(AeatEnvironment.TESTING,
            new InvoiceId("89890001K", "12345678&G33", LocalDate.of(2024, 1, 1)), new BigDecimal("241.4"));

    @Test
    void pngDecodesBackToTheSameUrlWithErrorCorrectionM() throws Exception {
        byte[] png = QrCode.of(URL, 300).toPng();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertThat(image).isNotNull();

        Result result = new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image))));
        assertThat(result.getText()).isEqualTo(URL.toString());
        assertThat(result.getResultMetadata().get(ResultMetadataType.ERROR_CORRECTION_LEVEL)).isEqualTo("M");
    }

    @Test
    void imageIsSquareAndRoughlyTheRequestedSize() {
        QrCode qr = QrCode.of(URL, 300);
        assertThat(qr.width()).isEqualTo(qr.height()).isBetween(300, 330);
    }

    @Test
    void hasALightQuietZoneAroundTheSymbol() {
        QrCode qr = QrCode.of(URL, 300);
        int w = qr.width();
        for (int i = 0; i < w; i++) {
            assertThat(qr.isDark(i, 0)).isFalse();
            assertThat(qr.isDark(i, w - 1)).isFalse();
            assertThat(qr.isDark(0, i)).isFalse();
            assertThat(qr.isDark(w - 1, i)).isFalse();
        }
    }

    @Test
    void rejectsSizesSmallerThanTheSmallestSymbol() {
        assertThatThrownBy(() -> QrCode.of(URL, 20)).isInstanceOf(IllegalArgumentException.class);
    }
}
