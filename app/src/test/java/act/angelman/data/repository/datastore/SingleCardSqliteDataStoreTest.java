package act.angelman.data.repository.datastore;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.List;

import act.angelman.BuildConfig;
import act.angelman.data.sqlite.CardColumns;
import act.angelman.data.sqlite.DatabaseHelper;
import act.angelman.domain.model.CardModel;
import lombok.Cleanup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk=22)
public class SingleCardSqliteDataStoreTest {
    private SQLiteDatabase mockDb;

    private String SQL_CREATE_SINGLECARD_LIST =
            "CREATE TABLE " + CardColumns.TABLE_NAME + "(" +
                    CardColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    CardColumns.CATEGORY_ID + " INTEGER," +
                    CardColumns.NAME + " TEXT," +
                    CardColumns.CONTENT_PATH + " TEXT," +
                    CardColumns.VOICE_PATH + " TEXT," +
                    CardColumns.FIRST_TIME + " TEXT," +
                    CardColumns.CARD_TYPE + " TEXT," +
                    CardColumns.THUMBNAIL_PATH + " TEXT," +
                    CardColumns.HIDE + " INTEGER," +
                    CardColumns.CARD_INDEX + " INTEGER)";
    private String[] columns = {CardColumns._ID, CardColumns.NAME, CardColumns.CONTENT_PATH,CardColumns.THUMBNAIL_PATH, CardColumns.VOICE_PATH, CardColumns.FIRST_TIME, CardColumns.CARD_TYPE, CardColumns.HIDE};
    private DatabaseHelper mockDbHelper;
    private SingleCardSqliteDataStore dataStore;

    @Before
    public void setUp() throws Exception {
        mockDb = SQLiteDatabase.create(null);
        mockDbHelper = mock(DatabaseHelper.class);
        dataStore = new SingleCardSqliteDataStore(RuntimeEnvironment.application);
        dataStore.dbHelper = mockDbHelper;

        when(mockDbHelper.getWritableDatabase()).thenReturn(mockDb);
        when(mockDbHelper.getReadableDatabase()).thenReturn(mockDb);
    }

    @Test
    public void givenExistDataBase_whenGetAllSingleCardList_thenVerifySizeOfList() throws Exception {

        mockDb.execSQL(SQL_CREATE_SINGLECARD_LIST);
        insertData(mockDb);

        @Cleanup
        Cursor c = mockDb.query(CardColumns.TABLE_NAME, columns, null,null, null, null, CardColumns.CARD_INDEX + " desc");
        List<CardModel> list = dataStore.getCardListWithCategoryId(0);

        verify(mockDbHelper).getReadableDatabase();

        assertThat(c.getCount()).isEqualTo(list.size());
        assertThat(list.get(0).name).isEqualTo("우유");
    }

    @Test
    public void givenExistDataBase_whenGetSingleCard_thenReturnSingleCardModel() throws Exception {

        mockDb.execSQL(SQL_CREATE_SINGLECARD_LIST);
        insertData(mockDb);

        @Cleanup
        Cursor c = mockDb.query(CardColumns.TABLE_NAME, columns, null,null, null, null, CardColumns.CARD_INDEX + " desc");
        CardModel cardModel = dataStore.getSingleCard("1");

        verify(mockDbHelper).getReadableDatabase();

        assertThat(cardModel._id).isEqualTo("1");
        assertThat(cardModel.name).isEqualTo("물 먹고 싶어요");
    }

    @Test
    public void givenExistDataBase_whenCreateSingleCard_thenVerifyIncrementation() throws Exception {

        mockDb.execSQL(SQL_CREATE_SINGLECARD_LIST);
        insertData(mockDb);

        @Cleanup
        Cursor c = mockDb.query(CardColumns.TABLE_NAME, columns, null,null, null, null, CardColumns.FIRST_TIME + " desc");

        int initialCount = c.getCount();

        CardModel cardModel = CardModel.builder().name("치킨").contentPath("chicken.png").firstTime("20161012_133600").cardType(CardModel.CardType.PHOTO_CARD).build();
        long result = dataStore.createSingleCardModel(cardModel);

        @Cleanup
        Cursor resultCursor = mockDb.query(CardColumns.TABLE_NAME, columns, null,null, null, null, CardColumns.CARD_INDEX + " desc");
        resultCursor.moveToFirst();
        assertThat(resultCursor.getString(resultCursor.getColumnIndex(CardColumns.NAME))).isEqualTo("치킨");
        assertThat(resultCursor.getCount()).isEqualTo(initialCount + 1);
        assertThat(result).isNotEqualTo(-1);
    }

    @Test
    public void givenExistDataBase_whenUpdateSingleCardModelHide_thenVerifyChangeHide() throws Exception {

        mockDb.execSQL(SQL_CREATE_SINGLECARD_LIST);
        insertData(mockDb);

        for(CardModel cardModel  : dataStore.getCardListWithCategoryId(0)){
            if(cardModel.cardIndex == 1){
                assertThat(cardModel.hide).isFalse();
            }
        }

        // when
        dataStore.updateSingleCardModelHide(0, 1, true);

        // then
        for(CardModel cardModel  : dataStore.getCardListWithCategoryId(0)){
            if(cardModel.cardIndex == 1){
                assertThat(cardModel.hide).isTrue();
            }
        }

    }

    @Test
    public void givenExistDataBase_whenUpdateSingleCardModelContent_thenVerifyChange() throws Exception {

        mockDb.execSQL(SQL_CREATE_SINGLECARD_LIST);
        insertData(mockDb);

        for(CardModel cardModel  : dataStore.getCardListWithCategoryId(0)){
            if("1".equals(cardModel._id)){
                assertThat(cardModel.cardType).isEqualTo(CardModel.CardType.VIDEO_CARD);
                assertThat(cardModel.contentPath).isEqualTo("water.mp4");
                assertThat(cardModel.thumbnailPath).isEqualTo("water.jpg");
            }
        }

        // when
        ContentValues contentValues = new ContentValues();
        contentValues.put(CardColumns.CARD_TYPE, CardModel.CardType.PHOTO_CARD.getValue());
        contentValues.put(CardColumns.CONTENT_PATH, "change.jpg");
        String thumbnailPath = null;
        contentValues.put(CardColumns.THUMBNAIL_PATH, thumbnailPath);
        dataStore.updateSingleCardModel("1", contentValues);

        // then
        for(CardModel cardModel  : dataStore.getCardListWithCategoryId(0)){
            if("1".equals(cardModel._id)){
                assertThat(cardModel.cardType).isEqualTo(CardModel.CardType.PHOTO_CARD);
                assertThat(cardModel.contentPath).isEqualTo("change.jpg");
                assertThat(cardModel.thumbnailPath).isNull();
            }
        }

    }

    @Test
    public void givenExistDataBase_whenRemoveSingleCardModel_thenVerifyChange() throws Exception {

        mockDb.execSQL(SQL_CREATE_SINGLECARD_LIST);
        insertData(mockDb);

        // when
        List<CardModel> listBefore = dataStore.getCardListWithCategoryId(0);
        assertThat(listBefore.size()).isEqualTo(3);

        dataStore.removeSingleCardModel(0, 0);

        // then
        List<CardModel> listAfter = dataStore.getCardListWithCategoryId(0);
        assertThat(listAfter.size()).isEqualTo(2);
    }

    @Test
    public void givenExistDataBase_whenRemoveSingleCardsInCategory_thenVerifyChange() throws Exception {

        mockDb.execSQL(SQL_CREATE_SINGLECARD_LIST);
        insertData(mockDb);

        // when
        List<CardModel> listBefore = dataStore.getCardListWithCategoryId(0);
        assertThat(listBefore.size()).isEqualTo(3);

        dataStore.removeSingleCardsInCategory(0);

        // then
        List<CardModel> listAfter = dataStore.getCardListWithCategoryId(0);
        assertThat(listAfter).isNull();
    }

    @Test
    public void givenExistDataBase_whenUpdateCategoryCardIndex_thenVerifyChange() throws Exception {

        mockDb.execSQL(SQL_CREATE_SINGLECARD_LIST);
        insertData(mockDb);

        // when
        List<CardModel> listBefore = dataStore.getCardListWithCategoryId(0);
        assertThat(listBefore.get(0).name).isEqualTo("우유");
        assertThat(listBefore.get(1).name).isEqualTo("쥬스");
        assertThat(listBefore.get(2).name).isEqualTo("물 먹고 싶어요");
        Collections.reverse(listBefore);
        dataStore.updateCategoryCardIndex(listBefore);

        // then
        List<CardModel> listAfter = dataStore.getCardListWithCategoryId(0);
        assertThat(listAfter.get(0).name).isEqualTo("물 먹고 싶어요");
        assertThat(listAfter.get(1).name).isEqualTo("쥬스");
        assertThat(listAfter.get(2).name).isEqualTo("우유");
    }

    private void insertData(SQLiteDatabase db){
        int index = 0;
        insertCategoryItemData(db, "1",   0       , "물 먹고 싶어요"  , "water.mp4", "water.jpg",  "20161018_000002", CardModel.CardType.VIDEO_CARD, index++);
        insertCategoryItemData(db, "2",   0       , "쥬스"          , "juice.png",null, "20161019_120018", CardModel.CardType.PHOTO_CARD, index++);
        insertCategoryItemData(db, "3",   0       , "우유"          , "milk.png", null, "20161019_120017", CardModel.CardType.PHOTO_CARD, index++);
    }

    private void insertCategoryItemData(SQLiteDatabase db, String id, int categoryIndex, String item, String imagePath,String thumbnailPath, String firstTime, CardModel.CardType cardType, int index){
        ContentValues contentValues = new ContentValues();
        contentValues.put(CardColumns._ID, id);
        contentValues.put(CardColumns.CATEGORY_ID, categoryIndex);
        contentValues.put(CardColumns.NAME, item);
        contentValues.put(CardColumns.CONTENT_PATH, imagePath);
        contentValues.put(CardColumns.THUMBNAIL_PATH, thumbnailPath);
        contentValues.put(CardColumns.FIRST_TIME, firstTime);
        contentValues.put(CardColumns.CARD_TYPE, cardType.getValue());
        contentValues.put(CardColumns.CARD_INDEX, index);
        contentValues.put(CardColumns.HIDE, 0);
        db.insert(CardColumns.TABLE_NAME, "null", contentValues);
    }
}