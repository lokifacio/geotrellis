package geotrellis.raster.mapalgebra.focal

import geotrellis.raster._
import geotrellis.raster.summary.Statistics
import geotrellis.raster.histogram.FastMapHistogram
import geotrellis.raster.mapalgebra.focal.TargetCell.TargetCell

/**
 * Calculates spatial autocorrelation of cells based on the similarity to
 * neighboring values.
 *
 * The statistic for each focus in the resulting raster is such that the more
 * positive the number, the greater the similarity between the focus value and
 * it's neighboring values, and the more negative the number, the more dissimilar
 * the focus value is with it's neighboring values.
 *
 * @note                  This operation requires that the whole raster be passed in;
 *                        it does not work over tiles.
 * @note                  Since mean and standard deviation are based off of an
 *                        Int based Histogram, those values will come from rounded values
 *                        of a double typed Tile (FloatConstantNoDataCellType, DoubleConstantNoDataCellType).
 */
object TileMoransICalculation {
  def apply(tile: Tile, n: Neighborhood, target: TargetCell = TargetCell.All, bounds: Option[GridBounds]): Tile = {
    new CursorCalculation[Tile](tile, n, target, bounds)
      with DoubleArrayTileResult {

      var mean = 0.0
      var `stddev^2` = 0.0

      val h = FastMapHistogram.fromTile(tile)
      val stats = h.statistics
      require(stats.nonEmpty)
      val Statistics(_, m, _, _, s, _, _) = stats.get
      mean = m
      `stddev^2` = s * s

      def calc(tile: Tile, cursor: Cursor) = {
        var z = 0.0
        var w = 0
        var base = 0.0

        cursor.allCells.foreach { (x, y) =>
          if (x == cursor.col && y == cursor.row) {
            base = tile.getDouble(x, y) - mean
          } else {
            z += tile.getDouble(x, y) - mean
            w += 1
          }
        }

        setValidDoubleResult(cursor, (base / `stddev^2` * z) / w)
      }
    }
  }.execute()
}

/**
 * Calculates global spatial autocorrelation of a raster based on the similarity to
 * neighboring values.
 *
 * The resulting statistic is such that the more positive the number, the greater
 * the similarity of values in the raster, and the more negative the number,
 * the more dissimilar the raster values are.
 *
 * @note                  This operation requires that the whole raster be passed in;
 *                        it does not work over tiles.
 * @note                  Since mean and standard deviation are based off of an
 *                        Int based Histogram, those values will come from rounded values
 *                        of a double typed Tile (FloatConstantNoDataCellType, DoubleConstantNoDataCellType).
 */
object ScalarMoransICalculation {
  def apply(tile: Tile, n: Neighborhood, bounds: Option[GridBounds]): Double = {
    new CursorCalculation[Double](tile, n, TargetCell.All, bounds)
    {
      var mean: Double = 0
      var `stddev^2`: Double = 0

      var count: Double = 0.0
      var ws: Int = 0

      val h = FastMapHistogram.fromTile(tile)
      val stats = h.statistics
      require(stats.nonEmpty)
      val Statistics(_, m, _, _, s, _, _) = stats.get
      mean = m
      `stddev^2` = s * s

      def calc(tile: Tile, cursor: Cursor) = {
        val base = tile.getDouble(cursor.col, cursor.row) - mean
        var z = -base

        cursor.allCells.foreach { (x, y) => z += tile.getDouble(x, y) - mean; ws += 1}

        count += base / `stddev^2` * z
        ws -= 1 // subtract one to account for focus
      }

      def result = count / ws

      def set(x: Int, y: Int, value: Int) {}

      def setDouble(x: Int, y: Int, value: Double) {}
    }
  }.execute()
}
