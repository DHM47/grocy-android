/*
 * This file is part of Grocy Android.
 *
 * Grocy Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Grocy Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Grocy Android. If not, see http://www.gnu.org/licenses/.
 *
 * Copyright (c) 2020-2021 by Patrick Zedler and Dominic Zedler
 */

package xyz.zedler.patrick.grocy.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import xyz.zedler.patrick.grocy.R;
import xyz.zedler.patrick.grocy.model.GroupedListItem;
import xyz.zedler.patrick.grocy.model.Product;
import xyz.zedler.patrick.grocy.model.ProductGroup;
import xyz.zedler.patrick.grocy.model.QuantityUnit;
import xyz.zedler.patrick.grocy.model.ShoppingListBottomNotes;
import xyz.zedler.patrick.grocy.model.ShoppingListItem;
import xyz.zedler.patrick.grocy.util.NumUtil;
import xyz.zedler.patrick.grocy.util.PluralUtil;

public class ShoppingModeItemAdapter extends
    RecyclerView.Adapter<ShoppingModeItemAdapter.ViewHolder> {

  private final LinearLayoutManager linearLayoutManager;
  private final ArrayList<GroupedListItem> groupedListItems;
  private final HashMap<Integer, Product> productHashMap;
  private final HashMap<Integer, QuantityUnit> quantityUnitHashMap;
  private final HashMap<Integer, Double> shoppingListItemAmountsHashMap;
  private final ArrayList<Integer> missingProductIds;
  private final ShoppingModeItemClickListener listener;
  private final PluralUtil pluralUtil;

  public ShoppingModeItemAdapter(
      Context context,
      LinearLayoutManager linearLayoutManager,
      ArrayList<GroupedListItem> groupedListItems,
      HashMap<Integer, Product> productHashMap,
      HashMap<Integer, QuantityUnit> quantityUnitHashMap,
      HashMap<Integer, Double> shoppingListItemAmountsHashMap,
      ArrayList<Integer> missingProductIds,
      ShoppingModeItemClickListener listener
  ) {
    this.linearLayoutManager = linearLayoutManager;
    this.groupedListItems = new ArrayList<>(groupedListItems);
    this.productHashMap = new HashMap<>(productHashMap);
    this.quantityUnitHashMap = new HashMap<>(quantityUnitHashMap);
    this.shoppingListItemAmountsHashMap = new HashMap<>(shoppingListItemAmountsHashMap);
    this.missingProductIds = new ArrayList<>(missingProductIds);
    this.listener = listener;
    pluralUtil = new PluralUtil(context);
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    private final MaterialCardView cardViewContainer;
    private final LinearLayout linearLayoutContainer;
    private final TextView textViewName;
    private final TextView textViewAmount;
    private final TextView textViewGroupName;
    private final TextView textViewNote;
    private final TextView textViewNoteName;
    private final TextView textViewBottomNotes;
    private final View viewGroupSeparator;

    public ViewHolder(View view) {
      super(view);
      cardViewContainer = view.findViewById(R.id.card_shopping_item_container);
      linearLayoutContainer = view.findViewById(R.id.linear_shopping_item_container);
      textViewName = view.findViewById(R.id.text_shopping_item_name);
      textViewAmount = view.findViewById(R.id.text_shopping_item_amount);
      textViewNote = view.findViewById(R.id.text_shopping_item_note);
      textViewNoteName = view.findViewById(R.id.text_shopping_note_as_name);
      textViewBottomNotes = view.findViewById(R.id.text_shopping_bottom_notes);
      textViewGroupName = view.findViewById(R.id.text_shopping_group_name);
      viewGroupSeparator = view.findViewById(R.id.separator);
    }
  }

  @Override
  public int getItemViewType(int position) {
    return groupedListItems.get(position).getType();
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == GroupedListItem.TYPE_HEADER) {
      return new ShoppingModeItemAdapter.ViewHolder(
          LayoutInflater.from(parent.getContext()).inflate(
              R.layout.row_shopping_group,
              parent,
              false
          )
      );
    } else if (viewType == GroupedListItem.TYPE_ENTRY) {
      return new ShoppingModeItemAdapter.ViewHolder(
          LayoutInflater.from(parent.getContext()).inflate(
              R.layout.row_shopping_item,
              parent,
              false
          )
      );
    } else {
      return new ShoppingModeItemAdapter.ViewHolder(
          LayoutInflater.from(parent.getContext()).inflate(
              R.layout.row_shopping_bottom_notes,
              parent,
              false
          )
      );
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public void onBindViewHolder(@NonNull final ViewHolder holder, int positionDoNotUse) {
    int position = holder.getAdapterPosition();

    GroupedListItem groupedListItem = groupedListItems.get(position);

    int type = getItemViewType(position);
    if (type == GroupedListItem.TYPE_HEADER) {
      String productGroupName = ((ProductGroup) groupedListItem).getName();
      holder.textViewGroupName.setText(productGroupName);
      if (!productGroupName.equals(holder.textViewGroupName.getContext()
          .getString(R.string.subtitle_done))
      ) {
        holder.textViewGroupName.setTextColor(ContextCompat.getColor(
            holder.textViewGroupName.getContext(), R.color.retro_green_fg
        ));
        holder.viewGroupSeparator.setBackgroundTintList(ColorStateList.valueOf(
            ContextCompat.getColor(
                holder.textViewGroupName.getContext(),
                R.color.retro_green_fg
            )
        ));
      } else {
        holder.textViewGroupName.setTextColor(ContextCompat.getColor(
            holder.textViewGroupName.getContext(), R.color.retro_yellow_fg
        ));
        holder.viewGroupSeparator.setBackgroundTintList(ColorStateList.valueOf(
            ContextCompat.getColor(
                holder.textViewGroupName.getContext(),
                R.color.retro_yellow_fg
            )
        ));
      }
      return;
    }
    if (type == GroupedListItem.TYPE_BOTTOM_NOTES) {
      holder.textViewBottomNotes.setText(
          ((ShoppingListBottomNotes) groupedListItem).getNotes()
      );
      holder.textViewBottomNotes.setOnClickListener(
          view -> listener.onItemRowClicked(groupedListItem)
      );
      return;
    }

    ShoppingListItem item = (ShoppingListItem) groupedListItem;

    // NAME

    Product product = null;
    if (item.hasProduct()) {
      product = productHashMap.get(item.getProductIdInt());
    }

    if (product != null) {
      holder.textViewName.setText(product.getName());
      holder.textViewName.setVisibility(View.VISIBLE);
    } else {
      holder.textViewName.setText(null);
      holder.textViewName.setVisibility(View.GONE);
    }
    if (item.isUndone()) {
      holder.textViewName.setPaintFlags(
          holder.textViewName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
      );
    } else {
      holder.textViewName.setPaintFlags(
          holder.textViewName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
      );
    }

    // NOTE AS NAME

    if (holder.textViewName.getVisibility() == View.VISIBLE) {
      holder.textViewNoteName.setVisibility(View.GONE);
      holder.textViewNoteName.setText(null);
    }

    // AMOUNT

    Double amountInQuUnit = shoppingListItemAmountsHashMap.get(item.getId());
    if (product != null && amountInQuUnit != null) {
      QuantityUnit quantityUnit = quantityUnitHashMap.get(item.getQuIdInt());
      String quStr = pluralUtil.getQuantityUnitPlural(quantityUnit, amountInQuUnit);
      if (quStr != null) {
        holder.textViewAmount.setText(
            holder.textViewAmount.getContext()
                .getString(R.string.subtitle_amount, NumUtil.trim(amountInQuUnit), quStr)
        );
      } else {
        holder.textViewAmount.setText(NumUtil.trim(amountInQuUnit));
      }
    } else if (product != null) {
      QuantityUnit quantityUnit = quantityUnitHashMap.get(product.getQuIdStockInt());
      String quStr = pluralUtil.getQuantityUnitPlural(quantityUnit, item.getAmountDouble());
      if (quStr != null) {
        holder.textViewAmount.setText(
            holder.textViewAmount.getContext()
                .getString(R.string.subtitle_amount, NumUtil.trim(item.getAmountDouble()), quStr)
        );
      } else {
        holder.textViewAmount.setText(NumUtil.trim(item.getAmountDouble()));
      }
    } else {
      holder.textViewAmount.setText(NumUtil.trim(item.getAmountDouble()));
    }

    if (item.isUndone()) {
      holder.textViewAmount.setPaintFlags(
          holder.textViewAmount.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
      );
    } else {
      holder.textViewAmount.setPaintFlags(
          holder.textViewAmount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
      );
    }

    // NOTE

    if (item.getNote() != null && !item.getNote().isEmpty()) {
      if (holder.textViewName.getVisibility() == View.VISIBLE) {
        holder.textViewNote.setVisibility(View.VISIBLE);
        holder.textViewNote.setText(item.getNote().trim());
      } else {
        holder.textViewNoteName.setVisibility(View.VISIBLE);
        holder.textViewNoteName.setText(item.getNote().trim());
        holder.textViewNote.setVisibility(View.GONE);
        holder.textViewNote.setText(null);
      }
    } else {
      if (holder.textViewName.getVisibility() == View.VISIBLE) {
        holder.textViewNote.setVisibility(View.GONE);
        holder.textViewNote.setText(null);
      }
    }
    if (holder.textViewNoteName.getVisibility() == View.VISIBLE) {
      if (item.isUndone()) {
        holder.textViewNoteName.setPaintFlags(
            holder.textViewNoteName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
        );
      } else {
        holder.textViewNoteName.setPaintFlags(
            holder.textViewNoteName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
        );
      }
    } else {
      if (item.isUndone()) {
        holder.textViewNote.setPaintFlags(
            holder.textViewNote.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
        );
      } else {
        holder.textViewNote.setPaintFlags(
            holder.textViewNote.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
        );
      }
    }

    holder.linearLayoutContainer.setAlpha(item.getDoneInt() == 1 ? 0.4f : 1);

    // CONTAINER

    holder.cardViewContainer.setOnClickListener(
        view -> listener.onItemRowClicked(groupedListItem)
    );
  }

  public void updateData(
      ArrayList<GroupedListItem> newList,
      HashMap<Integer, Product> productHashMap,
      HashMap<Integer, QuantityUnit> quantityUnitHashMap,
      HashMap<Integer, Double> shoppingListItemAmountsHashMap,
      ArrayList<Integer> missingProductIds
  ) {
    ShoppingModeItemAdapter.DiffCallback diffCallback = new ShoppingModeItemAdapter.DiffCallback(
        newList,
        this.groupedListItems,
        this.productHashMap,
        productHashMap,
        this.quantityUnitHashMap,
        quantityUnitHashMap,
        this.shoppingListItemAmountsHashMap,
        shoppingListItemAmountsHashMap,
        this.missingProductIds,
        missingProductIds
    );
    DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
    this.groupedListItems.clear();
    this.groupedListItems.addAll(newList);
    this.productHashMap.clear();
    this.productHashMap.putAll(productHashMap);
    this.quantityUnitHashMap.clear();
    this.quantityUnitHashMap.putAll(quantityUnitHashMap);
    this.shoppingListItemAmountsHashMap.clear();
    this.shoppingListItemAmountsHashMap.putAll(shoppingListItemAmountsHashMap);
    this.missingProductIds.clear();
    this.missingProductIds.addAll(missingProductIds);
    diffResult.dispatchUpdatesTo(new AdapterListUpdateCallback(this, linearLayoutManager));
  }

  static class DiffCallback extends DiffUtil.Callback {

    final ArrayList<GroupedListItem> oldItems;
    final ArrayList<GroupedListItem> newItems;
    final HashMap<Integer, Product> productHashMapOld;
    final HashMap<Integer, Product> productHashMapNew;
    final HashMap<Integer, QuantityUnit> quantityUnitHashMapOld;
    final HashMap<Integer, QuantityUnit> quantityUnitHashMapNew;
    HashMap<Integer, Double> shoppingListItemAmountsHashMapOld;
    HashMap<Integer, Double> shoppingListItemAmountsHashMapNew;
    final ArrayList<Integer> missingProductIdsOld;
    final ArrayList<Integer> missingProductIdsNew;

    public DiffCallback(
        ArrayList<GroupedListItem> newItems,
        ArrayList<GroupedListItem> oldItems,
        HashMap<Integer, Product> productHashMapOld,
        HashMap<Integer, Product> productHashMapNew,
        HashMap<Integer, QuantityUnit> quantityUnitHashMapOld,
        HashMap<Integer, QuantityUnit> quantityUnitHashMapNew,
        HashMap<Integer, Double> shoppingListItemAmountsHashMapOld,
        HashMap<Integer, Double> shoppingListItemAmountsHashMapNew,
        ArrayList<Integer> missingProductIdsOld,
        ArrayList<Integer> missingProductIdsNew
    ) {
      this.newItems = newItems;
      this.oldItems = oldItems;
      this.productHashMapOld = productHashMapOld;
      this.productHashMapNew = productHashMapNew;
      this.quantityUnitHashMapOld = quantityUnitHashMapOld;
      this.quantityUnitHashMapNew = quantityUnitHashMapNew;
      this.shoppingListItemAmountsHashMapOld = shoppingListItemAmountsHashMapOld;
      this.shoppingListItemAmountsHashMapNew = shoppingListItemAmountsHashMapNew;
      this.missingProductIdsOld = missingProductIdsOld;
      this.missingProductIdsNew = missingProductIdsNew;
    }

    @Override
    public int getOldListSize() {
      return oldItems.size();
    }

    @Override
    public int getNewListSize() {
      return newItems.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
      return compare(oldItemPosition, newItemPosition, false);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
      return compare(oldItemPosition, newItemPosition, true);
    }

    private boolean compare(int oldItemPos, int newItemPos, boolean compareContent) {
      int oldItemType = oldItems.get(oldItemPos).getType();
      int newItemType = newItems.get(newItemPos).getType();
      if (oldItemType != newItemType) {
        return false;
      }
      if (oldItemType == GroupedListItem.TYPE_ENTRY) {
        ShoppingListItem newItem = (ShoppingListItem) newItems.get(newItemPos);
        ShoppingListItem oldItem = (ShoppingListItem) oldItems.get(oldItemPos);
        if (!compareContent) {
          return newItem.getId() == oldItem.getId();
        }

        Integer productIdOld =
            NumUtil.isStringInt(oldItem.getProductId()) ? Integer.parseInt(oldItem.getProductId())
                : null;
        Product productOld = productIdOld != null ? productHashMapOld.get(productIdOld) : null;

        Integer productIdNew =
            NumUtil.isStringInt(newItem.getProductId()) ? Integer.parseInt(newItem.getProductId())
                : null;
        Product productNew = productIdNew != null ? productHashMapNew.get(productIdNew) : null;

        Integer quIdOld =
            NumUtil.isStringInt(oldItem.getQuId()) ? Integer.parseInt(oldItem.getQuId()) : null;
        QuantityUnit quOld = quIdOld != null ? quantityUnitHashMapOld.get(quIdOld) : null;

        Integer quIdNew =
            NumUtil.isStringInt(newItem.getQuId()) ? Integer.parseInt(newItem.getQuId()) : null;
        QuantityUnit quNew = quIdNew != null ? quantityUnitHashMapNew.get(quIdNew) : null;

        Double amountOld = shoppingListItemAmountsHashMapOld.get(oldItem.getId());
        Double amountNew = shoppingListItemAmountsHashMapNew.get(newItem.getId());

        Boolean missingOld =
            productIdOld != null ? missingProductIdsOld.contains(productIdOld) : null;
        Boolean missingNew =
            productIdNew != null ? missingProductIdsNew.contains(productIdNew) : null;

        if (productOld == null && productNew != null
            || productOld != null && productNew != null && productOld.getId() != productNew.getId()
            || quOld == null && quNew != null
            || quOld != null && quNew != null && quOld.getId() != quNew.getId()
            || !Objects.equals(amountOld, amountNew)
            || missingOld == null && missingNew != null
            || missingOld != null && missingNew != null && missingOld != missingNew
        ) {
          return false;
        }

        return newItem.equals(oldItem);
      } else if (oldItemType == GroupedListItem.TYPE_HEADER) {
        ProductGroup newGroup = (ProductGroup) newItems.get(newItemPos);
        ProductGroup oldGroup = (ProductGroup) oldItems.get(oldItemPos);
        return newGroup.equals(oldGroup);
      } else { // Type: Bottom notes
        ShoppingListBottomNotes newNotes = (ShoppingListBottomNotes) newItems.get(newItemPos);
        ShoppingListBottomNotes oldNotes = (ShoppingListBottomNotes) oldItems.get(oldItemPos);
        return newNotes.equals(oldNotes);
      }
    }
  }

  @Override
  public int getItemCount() {
    return groupedListItems.size();
  }

  public interface ShoppingModeItemClickListener {

    void onItemRowClicked(GroupedListItem groupedListItem);
  }

  /**
   * Custom ListUpdateCallback that prevents RecyclerView from scrolling down if top item is moved.
   */
  public static final class AdapterListUpdateCallback implements ListUpdateCallback {

    @NonNull
    private final ShoppingModeItemAdapter mAdapter;
    private final LinearLayoutManager linearLayoutManager;

    public AdapterListUpdateCallback(
        @NonNull ShoppingModeItemAdapter adapter,
        LinearLayoutManager linearLayoutManager
    ) {
      this.mAdapter = adapter;
      this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onInserted(int position, int count) {
      mAdapter.notifyItemRangeInserted(position, count);
    }

    @Override
    public void onRemoved(int position, int count) {
      mAdapter.notifyItemRangeRemoved(position, count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
      // workaround for https://github.com/patzly/grocy-android/issues/439
      // figure out the position of the first visible item
      int firstPos = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
      int offsetTop = 0;
      if(firstPos >= 0) {
        View firstView = linearLayoutManager.findViewByPosition(firstPos);
        if (firstView != null) {
          offsetTop = linearLayoutManager.getDecoratedTop(firstView)
              - linearLayoutManager.getTopDecorationHeight(firstView);
        }
      }

      mAdapter.notifyItemMoved(fromPosition, toPosition);

      // reapply the saved position
      if(firstPos >= 0) {
        linearLayoutManager.scrollToPositionWithOffset(firstPos, offsetTop);
      }
    }

    @Override
    public void onChanged(int position, int count, Object payload) {
      mAdapter.notifyItemRangeChanged(position, count, payload);
    }
  }
}
