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

package xyz.zedler.patrick.grocy.bottomappbar;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.EdgeTreatment;
import com.google.android.material.shape.ShapePath;

/**
 * Modification of the official Material Components' BottomAppBarTopEdgeTreatment.
 * <p>
 * Top edge treatment for the bottom app bar which "cradles" a circular {@link
 * FloatingActionButton}.
 *
 * <p>This edge features a downward semi-circular cutout from the edge line. The two corners
 * created
 * by the cutout can optionally be rounded. The circular cutout can also support a vertically offset
 * FloatingActionButton; i.e., the cut-out need not be a perfect semi-circle, but could be an arc of
 * less than 180 degrees that does not start or finish with a vertical path. This vertical offset
 * must be positive.
 */
public class BottomAppBarTopEdgeTreatment extends EdgeTreatment implements Cloneable {

  private static final int ARC_QUARTER = 90;
  private static final int ARC_HALF = 180;
  private static final int ANGLE_UP = 270;
  private static final int ANGLE_LEFT = 180;

  private float roundedCornerRadius;
  private float fabMargin;

  private float fabDiameter;
  private float cradleVerticalOffset;
  private float horizontalOffset;

  /**
   * @param fabMargin            the margin in pixels between the cutout and the fab.
   * @param roundedCornerRadius  the radius, in pixels, of the rounded corners created by the
   *                             cutout. A value of 0 will produce a sharp cutout.
   * @param cradleVerticalOffset vertical offset, in pixels, of the {@link FloatingActionButton}
   *                             being cradled. An offset of 0 indicates the vertical center of the
   *                             {@link FloatingActionButton} is positioned on the top edge.
   */
  public BottomAppBarTopEdgeTreatment(
      float fabMargin, float roundedCornerRadius, float cradleVerticalOffset) {
    this.fabMargin = fabMargin;
    this.roundedCornerRadius = roundedCornerRadius;
    setCradleVerticalOffset(cradleVerticalOffset);
    this.horizontalOffset = 0f;
  }

  @Override
  public void getEdgePath(
      float length, float center, float interpolation, @NonNull ShapePath shapePath) {
    if (fabDiameter == 0) {
      // There is no cutout to draw.
      shapePath.lineTo(length, 0);
      return;
    }

    float cradleDiameter = fabMargin * 2 + fabDiameter;
    float cradleRadius = cradleDiameter / 2f;
    float roundedCornerOffset = interpolation * roundedCornerRadius;
    float middle = center + horizontalOffset;

    // The center offset of the cutout tweens between the vertical offset when attached, and the
    // cradleRadius as it becomes detached.
    float verticalOffset =
        interpolation * cradleVerticalOffset + (1 - interpolation) * cradleRadius;
    float verticalOffsetRatio = verticalOffset / cradleRadius;
    if (verticalOffsetRatio >= 1.0f) {
      // Vertical offset is so high that there's no curve to draw in the edge, i.e., the fab is
      // actually above the edge so just draw a straight line.
      shapePath.lineTo(length, 0);
      return; // Early exit.
    }

    // Calculate the path of the cutout by calculating the location of two adjacent circles. One
    // circle is for the rounded corner. If the rounded corner circle radius is 0 the corner will
    // not be rounded. The other circle is the cutout.

    // Calculate the X distance between the center of the two adjacent circles using pythagorean
    // theorem.
    float distanceBetweenCenters = cradleRadius + roundedCornerOffset;
    float distanceBetweenCentersSquared = distanceBetweenCenters * distanceBetweenCenters;
    float distanceY = verticalOffset + roundedCornerOffset;
    float distanceX = (float) Math.sqrt(distanceBetweenCentersSquared - (distanceY * distanceY));

    // Calculate the x position of the rounded corner circles.
    float leftRoundedCornerCircleX = middle - distanceX;
    float rightRoundedCornerCircleX = middle + distanceX;

    // Calculate the arc between the center of the two circles.
    float cornerRadiusArcLength = (float) Math.toDegrees(Math.atan(distanceX / distanceY));
    float cutoutArcOffset = ARC_QUARTER - cornerRadiusArcLength;

    // Draw the starting line up to the left rounded corner.
    shapePath.lineTo(/* x= */ leftRoundedCornerCircleX, /* y= */ 0);

    // Draw the arc for the left rounded corner circle. The bounding box is the area around the
    // circle's center which is at `(leftRoundedCornerCircleX, roundedCornerOffset)`.
    shapePath.addArc(
        /* left= */ leftRoundedCornerCircleX - roundedCornerOffset,
        /* top= */ 0,
        /* right= */ leftRoundedCornerCircleX + roundedCornerOffset,
        /* bottom= */ roundedCornerOffset * 2,
        /* startAngle= */ ANGLE_UP,
        /* sweepAngle= */ cornerRadiusArcLength);

    // Draw the cutout circle.
    shapePath.addArc(
        /* left= */ middle - cradleRadius,
        /* top= */ -cradleRadius - verticalOffset,
        /* right= */ middle + cradleRadius,
        /* bottom= */ cradleRadius - verticalOffset,
        /* startAngle= */ ANGLE_LEFT - cutoutArcOffset,
        /* sweepAngle= */ cutoutArcOffset * 2 - ARC_HALF);

    // Draw an arc for the right rounded corner circle. The bounding box is the area around the
    // circle's center which is at `(rightRoundedCornerCircleX, roundedCornerOffset)`.
    shapePath.addArc(
        /* left= */ rightRoundedCornerCircleX - roundedCornerOffset,
        /* top= */ 0,
        /* right= */ rightRoundedCornerCircleX + roundedCornerOffset,
        /* bottom= */ roundedCornerOffset * 2,
        /* startAngle= */ ANGLE_UP - cornerRadiusArcLength,
        /* sweepAngle= */ cornerRadiusArcLength);

    // Draw the ending line after the right rounded corner.
    shapePath.lineTo(/* x= */ length, /* y= */ 0);
  }

  /**
   * Returns current fab diameter in pixels.
   *
   */
  @RestrictTo(LIBRARY_GROUP)
  public float getFabDiameter() {
    return fabDiameter;
  }

  /**
   * Sets the fab diameter the size of the fab in pixels.
   *
   */
  @RestrictTo(LIBRARY_GROUP)
  public void setFabDiameter(float fabDiameter) {
    this.fabDiameter = fabDiameter;
  }

  /**
   * Sets the horizontal offset, in pixels, of the cradle from center.
   */
  void setHorizontalOffset(float horizontalOffset) {
    this.horizontalOffset = horizontalOffset;
  }

  /**
   * Returns the horizontal offset, in pixels, of the cradle from center.
   *
   */
  @RestrictTo(LIBRARY_GROUP)
  public float getHorizontalOffset() {
    return horizontalOffset;
  }

  /**
   * Returns vertical offset, in pixels, of the {@link FloatingActionButton} being cradled. An
   * offset of 0 indicates the vertical center of the {@link FloatingActionButton} is positioned on
   * the top edge.
   */
  float getCradleVerticalOffset() {
    return cradleVerticalOffset;
  }

  /**
   * Sets the vertical offset, in pixels, of the {@link FloatingActionButton} being cradled. An
   * offset of 0 indicates the vertical center of the {@link FloatingActionButton} is positioned on
   * the top edge.
   */
  void setCradleVerticalOffset(@FloatRange(from = 0f) float cradleVerticalOffset) {
    if (cradleVerticalOffset < 0) {
      throw new IllegalArgumentException("cradleVerticalOffset must be positive.");
    }
    this.cradleVerticalOffset = cradleVerticalOffset;
  }

  float getFabCradleMargin() {
    return fabMargin;
  }

  void setFabCradleMargin(float fabMargin) {
    this.fabMargin = fabMargin;
  }

  float getFabCradleRoundedCornerRadius() {
    return roundedCornerRadius;
  }

  void setFabCradleRoundedCornerRadius(float roundedCornerRadius) {
    this.roundedCornerRadius = roundedCornerRadius;
  }
}
