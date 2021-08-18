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

package xyz.zedler.patrick.grocy.util;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TextUtil {

  private final static String TAG = TextUtil.class.getSimpleName();

  public static CharSequence trimCharSequence(CharSequence source) {
    if (source == null || source.length() == 0) {
      return null;
    }
    int i = 0;
    try {
      while (i < source.length() && Character.isWhitespace(source.charAt(i))) {
        i++;
      }
      int j = source.length() - 1;
      while (j >= 0 && j > i && Character.isWhitespace(source.charAt(j))) {
        j--;
      }
      return source.subSequence(i, j + 1);
    } catch (StringIndexOutOfBoundsException e) {
      return null;
    }
  }

  @NonNull
  public static String getRawText(Context context, @RawRes int resId) {
    InputStream inputStream = context.getResources().openRawResource(resId);
    BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(inputStream));
    StringBuilder text = new StringBuilder();
    try {
      for (String line; (line = bufferedReader.readLine()) != null; ) {
        text.append(line).append('\n');
      }
      text.deleteCharAt(text.length() - 1);
      inputStream.close();
    } catch (Exception e) {
      Log.e(TAG, "getRawText: ", e);
    }
    return text.toString();
  }
}
