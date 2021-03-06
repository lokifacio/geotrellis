/*
 * Copyright (c) 2014 Azavea.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package geotrellis.engine.op.focal

import geotrellis.engine._
import geotrellis.raster._
import geotrellis.raster.mapalgebra.focal.TargetCell.TargetCell
import geotrellis.raster.mapalgebra.focal._

@deprecated("geotrellis-engine has been deprecated", "Geotrellis Version 0.10")
trait FocalRasterSourceMethods extends RasterSourceMethods with FocalOperation {
  def focalSum(n: Neighborhood, target: TargetCell = TargetCell.All) = focal(n){ (tile, n, bounds) => Sum(tile, n, target, bounds) }
  def focalMin(n: Neighborhood, target: TargetCell = TargetCell.All) = focal(n){ (tile, n, bounds) => Min(tile, n, target, bounds) }
  def focalMax(n: Neighborhood, target: TargetCell = TargetCell.All) = focal(n){ (tile, n, bounds) => Max(tile, n, target, bounds) }
  def focalMean(n: Neighborhood, target: TargetCell = TargetCell.All) = focal(n){ (tile, n, bounds) => Mean(tile, n, target, bounds) }
  def focalMedian(n: Neighborhood, target: TargetCell = TargetCell.All) = focal(n){ (tile, n, bounds) => Median(tile, n, target, bounds) }
  def focalMode(n: Neighborhood, target: TargetCell = TargetCell.All) = focal(n){ (tile, n, bounds) => Mode(tile, n, target, bounds) }
  def focalStandardDeviation(n: Neighborhood, target: TargetCell = TargetCell.All) = focal(n){ (tile, n, bounds) => StandardDeviation(tile, n, target, bounds) }
  def focalConway() = focal(Square(1))(Conway.apply)

  def tileMoransI(n: Neighborhood, target: TargetCell = TargetCell.All) =
    rasterSource.globalOp(TileMoransICalculation.apply(_, n, target, None))

  def scalarMoransI(n: Neighborhood): ValueSource[Double] = {
    rasterSource.converge.map(ScalarMoransICalculation(_, n, None))
  }
}
