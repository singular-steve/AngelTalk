package act.sds.samsung.angelman.presentation.activity;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import act.sds.samsung.angelman.BuildConfig;
import act.sds.samsung.angelman.R;
import act.sds.samsung.angelman.UITest;

import static act.sds.samsung.angelman.presentation.activity.VideoActivity.VIDEO_FRAGMENT_TAG;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class VideoFragmentTest extends UITest {


    VideoActivity activity;
    private VideoFragment subject;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.setupActivity(VideoActivity.class);
        subject = new VideoFragment();
        activity.getFragmentManager().beginTransaction()
                .add(R.id.container, subject, VIDEO_FRAGMENT_TAG)
                .commit();
    }

    @Test
    public void whenShowVideoScreen_thenShowsRecordingArea() throws Exception{
        View recordingArea = subject.getView().findViewById(R.id.camera_frame);
        assertThat(recordingArea.getVisibility()).isEqualTo(View.VISIBLE);
    }


    @Test
    public void whenShowVideoScreen_thenShowsRecordingButton() throws Exception{
        ImageView recordingButton = (ImageView)subject.getView().findViewById(R.id.btn_record);
        assertThat(recordingButton.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void whenShowVideoScreen_thenShowGuideMessage() throws Exception{
        TextView guideMessage = (TextView)subject.getView().findViewById(R.id.video_guide);
        assertThat(guideMessage.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(guideMessage.getText()).isEqualTo("3초간 영상을 찍어보세요");
    }
}