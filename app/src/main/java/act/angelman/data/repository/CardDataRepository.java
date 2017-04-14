package act.angelman.data.repository;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.collect.Lists;

import java.util.List;

import act.angelman.data.repository.datastore.SingleCardDataStore;
import act.angelman.data.repository.datastore.SingleCardSqliteDataStore;
import act.angelman.domain.model.CardModel;
import act.angelman.domain.repository.CardRepository;

public class CardDataRepository implements CardRepository {

    private Context context;
    public CardDataRepository(Context context) {
        this.context = context;
    }


    @Override
    public long createSingleCardModel(CardModel cardModel) {
        SingleCardDataStore dataStore = new SingleCardSqliteDataStore(context);
        return dataStore.createSingleCardModel(cardModel);
    }

    @Override
    public List<CardModel> getSingleCardListWithCategoryId(int selectedCategoryId) {
        SingleCardDataStore dataStore = new SingleCardSqliteDataStore(context);
        return getDataModels(dataStore.getCardListWithCategoryId(selectedCategoryId));
    }

    @Override
    public List<CardModel> getSingleCardListWithCategoryId(int selectedCategoryId, boolean isHide) {
        SingleCardDataStore dataStore = new SingleCardSqliteDataStore(context);
        List<CardModel> lists = getDataModels(dataStore.getCardListWithCategoryId(selectedCategoryId));
        return extractCardListByHide(isHide, lists);
    }

    @NonNull
    public List<CardModel> extractCardListByHide(boolean isHide, List<CardModel> lists) {
        List<CardModel> results = Lists.newArrayList();
        for(CardModel cardModel : lists){
            if(cardModel.hide == isHide){
                results.add(cardModel);
            }
        }
        return results;
    }

    @Override
    public boolean deleteSingleCardsWithCategory(int category) {
        SingleCardDataStore dataStore = new SingleCardSqliteDataStore(context);
        return dataStore.removeSingleCardsInCategory(category);
    }

    @Override
    public boolean deleteSingleCardWithCardIndex(int categoryId, int cardIndex) {
        SingleCardDataStore dataStore = new SingleCardSqliteDataStore(context);
        return dataStore.removeSingleCardModel(categoryId, cardIndex);
    }

    @Override
    public boolean updateSingleCardModelHide(CardModel cardModel) {
        SingleCardDataStore dataStore = new SingleCardSqliteDataStore(context);
        return dataStore.updateSingleCardModelHide(cardModel.categoryId, cardModel.cardIndex, cardModel.hide);
    }

    @Override
    public boolean updateCategoryCardIndex(List<CardModel> cardModelList) {
        SingleCardDataStore dataStore = new SingleCardSqliteDataStore(context);
        return dataStore.updateCategoryCardIndex(cardModelList);
    }

    private List<CardModel> getDataModels(List <CardModel> cardModels){
        if(cardModels == null){
            return Lists.newArrayList();
        }else{
            return cardModels;
        }
    }
}