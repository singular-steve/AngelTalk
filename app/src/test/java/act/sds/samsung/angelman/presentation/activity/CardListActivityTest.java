package act.sds.samsung.angelman.presentation.activity;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import act.sds.samsung.angelman.BuildConfig;
import act.sds.samsung.angelman.R;
import act.sds.samsung.angelman.TestAngelmanApplication;
import act.sds.samsung.angelman.UITest;
import act.sds.samsung.angelman.domain.model.CardModel;
import act.sds.samsung.angelman.domain.model.CategoryModel;
import act.sds.samsung.angelman.domain.repository.CardRepository;
import act.sds.samsung.angelman.presentation.manager.ApplicationManager;
import act.sds.samsung.angelman.presentation.util.ContentsUtil;
import act.sds.samsung.angelman.presentation.util.ResourcesUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class CardListActivityTest extends UITest{

    @Inject
    ApplicationManager applicationManager;

    @Inject
    CardRepository cardRepository;

    private CardListActivity subject;

    @Before
    public void setUp() throws Exception {
        ((TestAngelmanApplication) RuntimeEnvironment.application).getAngelmanTestComponent().inject(this);
        when(applicationManager.getCategoryModel()).thenReturn(getCategoryModel());
        when(cardRepository.getSingleCardListWithCategoryId(applicationManager.getCategoryModel().index)).thenReturn(getCardModelList());
        subject = setupActivity(CardListActivity.class);
    }

    @Test
    public void whenLaunched_thenShowViewCorrectly() throws Exception {
        assertThat(shadowOf(subject.titleLayout.getBackground()).getCreatedFromResId()).isEqualTo(R.color.simple_background_red);
        assertThat(subject.categoryItemTitle.getText()).isEqualTo("먹을 것");
        assertThat(subject.cardListRecyclerView).isNotNull();
        assertThat(subject.cardListRecyclerView.getChildCount()).isEqualTo(8);
    }

    @Test
    public void whenClickBackButton_thenFinishActivity() throws Exception {
        subject.findViewById(R.id.back_button).performClick();
        assertThat(subject.isFinishing()).isTrue();
    }

    private CategoryModel getCategoryModel() {
        CategoryModel categoryModel = new CategoryModel();
        categoryModel.index = 0;
        categoryModel.title = "먹을 것";
        categoryModel.color = ResourcesUtil.RED;
        return categoryModel;
    }

    private List<CardModel> getCardModelList() {
        List<CardModel> list = Lists.newArrayList();

        String contentFolder = ContentsUtil.getContentFolder() + File.separator;

        list.add(new CardModel("물0", contentFolder+"water.png", "20161018_000003", 0, 0, CardModel.CardType.PHOTO_CARD));
        list.add(new CardModel("물1", contentFolder+"water.png", "20161018_000003", 0, 1, CardModel.CardType.PHOTO_CARD));
        list.add(new CardModel("물2", contentFolder+"water.png", "20161018_000003", 0, 2, CardModel.CardType.PHOTO_CARD));
        list.add(new CardModel("물3", contentFolder+"water.png", "20161018_000003", 0, 3, CardModel.CardType.PHOTO_CARD));
        list.add(new CardModel("물4", contentFolder+"water.png", "20161018_000003", 0, 4, CardModel.CardType.PHOTO_CARD));
        list.add(new CardModel("물5", contentFolder+"water.png", "20161018_000003", 0, 5, CardModel.CardType.PHOTO_CARD));
        list.add(new CardModel("물6", contentFolder+"water.png", "20161018_000003", 0, 6, CardModel.CardType.PHOTO_CARD));
        list.add(new CardModel("젤리", contentFolder+"haribo.mp4", "20161018_000003", 0, 7, CardModel.CardType.VIDEO_CARD, contentFolder+"haribo.jpg"));
        return list;
    }
}