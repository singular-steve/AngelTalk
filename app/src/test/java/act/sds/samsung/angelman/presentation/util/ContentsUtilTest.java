package act.sds.samsung.angelman.presentation.util;

import android.graphics.Bitmap;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;

import act.sds.samsung.angelman.BuildConfig;
import act.sds.samsung.angelman.domain.model.CardModel;
import act.sds.samsung.angelman.domain.model.CardTransferModel;
import act.sds.samsung.angelman.presentation.shadow.ShadowThumbnailUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, shadows = ShadowThumbnailUtil.class)
public class ContentsUtilTest {

    @Test
    public void givenFileNameAndBitMapExists_whenCallImageSave_thenSaveImage() throws Exception {
        String fileName = ContentsUtil.getImagePath();
        Bitmap fakeBitmap = Bitmap.createBitmap(1440, 2560, Bitmap.Config.ARGB_8888);//mock(Bitmap.class);
        Window window = mock(Window.class);
        WindowManager windowManager = mock(WindowManager.class);
        Display display = mock(Display.class);
        View view = mock(View.class);
        when(window.getWindowManager()).thenReturn(windowManager);
        when(windowManager.getDefaultDisplay()).thenReturn((display));
        when(window.getDecorView()).thenReturn(view);

        ContentsUtil.saveImage(fakeBitmap, fileName, 444, 112);

        assertThat(fakeBitmap.getHeight()).isEqualTo(2560);
        assertThat(fakeBitmap.getWidth()).isEqualTo(1440);

        File file = new File(fileName);
        assertThat(file.exists()).isTrue();
    }

    @Test
    public void givenFileFullPath_whenGetFileNameFromFullPath_thenReturnFileName() throws Exception {
        // given
        String fileFullPath = "/storage/emulated/0/angelman/DCIM/20170329_133245.jpg";
        // when then
        assertThat(ContentsUtil.getContentNameFromContentPath(fileFullPath)).isEqualTo("20170329_133245.jpg");
    }

    @Test
    public void givenVideoFile_whenCallSaveVideoThumbnail_thenMakeVideoThumbnailImage() throws Exception {
        // given
        FileUtil.copyFile(new File(ContentsUtil.getContentFolder() + File.separator + "airplane.mp4"), new File(ContentsUtil.getTempFolder() + File.separator + "airplane.mp4"));

        // when
        ContentsUtil.saveVideoThumbnail(ContentsUtil.getTempFolder() + File.separator + "airplane.mp4");

        // then
        assertThat(new File(ContentsUtil.getTempFolder() + File.separator + "airplane.jpg").exists()).isTrue();
    }

    @Test
    public void givenSharedPhotoCard_whenCallGetTempCardModel_thenMakeTempCardModel() throws Exception {
        // given
        CardTransferModel cardTransferModel = new CardTransferModel();
        cardTransferModel.name = "sharedPhotoCard";
        cardTransferModel.cardType = "PHOTO_CARD";

        String folderPath = ContentsUtil.getTempFolder() + File.separator;
        File content = new File(folderPath + "test.jpg");
        File voice = new File(folderPath + "test.3gdp");
        content.createNewFile();
        voice.createNewFile();

        // when
        CardModel cardModel = ContentsUtil.getTempCardModel(folderPath, cardTransferModel);

        // then
        assertThat(cardModel.name).isEqualTo("sharedPhotoCard");
        assertThat(cardModel.cardType).isEqualTo(CardModel.CardType.PHOTO_CARD);
        assertThat(cardModel.contentPath).isEqualTo(content.getAbsolutePath());
        assertThat(cardModel.voicePath).isEqualTo(voice.getAbsolutePath());
    }

    @Test
    public void givenSharedVideoCard_whenCallGetTempCardModel_thenMakeTempCardModel() throws Exception {
        // given
        CardTransferModel cardTransferModel = new CardTransferModel();
        cardTransferModel.name = "sharedVideoCard";
        cardTransferModel.cardType = "VIDEO_CARD";

        String folderPath = ContentsUtil.getTempFolder() + File.separator;
        File content = new File(folderPath + "test.mp4");
        File thumbnail = new File(folderPath + "test.jpg");
        File voice = new File(folderPath + "test.3gdp");
        content.createNewFile();
        thumbnail.createNewFile();
        voice.createNewFile();

        // when
        CardModel cardModel = ContentsUtil.getTempCardModel(folderPath, cardTransferModel);

        // then
        assertThat(cardModel.name).isEqualTo("sharedVideoCard");
        assertThat(cardModel.cardType).isEqualTo(CardModel.CardType.VIDEO_CARD);
        assertThat(cardModel.contentPath).isEqualTo(content.getAbsolutePath());
        assertThat(cardModel.thumbnailPath).isEqualTo(thumbnail.getAbsolutePath());
        assertThat(cardModel.voicePath).isEqualTo(voice.getAbsolutePath());
    }
}