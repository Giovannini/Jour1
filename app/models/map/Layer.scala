package models.map

/**
 * Model class for layer
 */
case class Layer(matrix: Array[Array[Int]], persist: Float) {

  private val size = matrix.length

  /**
   * Display a matrix
   * @author Thomas Giovannini, Simon Ronciere
   * @return The matrix in a string
   */
  private def printMatrix: String = {
    matrix.toList
      .map(_.toList
      .map(prettify)
      .mkString(""))
      .mkString("\n")
  }

  /**
   * Format matrix square
   * @author Simon Ronciere
   * @param number value of the tile
   * @return The square in html
   */
  private def prettify(number: Int): String = {
    "<span style=\"width=5px;height=5px;"+{if(number<100) "background-color:blue" else "background-color:rgb("+(255-number)+","+(255-number)+","+(255-number)+")"}+"\">"+"" + "0" +"</span>"
  }

  /**
   * Format matrix square
   * @author Thomas Giovannini
   * @return The square in html
   */
  override def toString: String = printMatrix

  /**
   * Method to add two layer (BE CAREFUL, not a real matrix sum).
   * @author Thomas GIOVANNINI
   * @param other the layer to add
   * @return a layer, sum of these two
   */
  def +(other: Layer): Layer = {
    val matrix = (this.matrix, other.matrix).zipped
      .map { (line1, line2) =>
        (line1, line2).zipped.map{(value1, value2) =>
          (value1 + value2 * other.persist).toInt}
    }
    Layer(matrix, this.persist)
  }

  /**
   * Method to divise a Layer by a number
   * @author Thomas GIOVANNINI
   * @param value with which the layer is divided
   * @return a layer with the matrix' values divided by the value in parameter
   */
  def /(value: Double): Layer = {
    val matrix = this.matrix.map(_.map(v => (v.toFloat / value).toInt))
    Layer(matrix, this.persist)
  }
}

object Layer {
  /**
   * Create a layer with a matrix filled with 0
   * @author Thomas GIOVANNINI
   * @param size size of the layer to create
   * @param persistence of the layer to create
   * @return a new layer
   */
  def filledWith0(size: Int, persistence: Float): Layer = {
    val matrix = Array.fill[Int](size, size)(0)
    Layer(matrix, persistence)
  }

  /**
   * Create a layer with a matrix filled with random values between 0 and 256
   * @author Thomas GIOVANNINI
   * @param size size of the layer to create
   * @param persistence of the layer to create
   * @return a new layer
   */
  def filledWithRandom(size: Int, persistence: Float): Layer = {
    val random = scala.util.Random
    val matrix = Array.fill[Int](size, size)(random.nextInt(256))
    Layer(matrix, persistence)
  }

  /**
   * Sum the working layers according to the Perlin algorithm
   * @author Simon RONCIERE
   * @param workingLayers list of working layer
   * @return a layer build with working layers
   */
  def sumLayers(workingLayers:List[Layer]):Layer={
    val octaves = workingLayers.length
    val size = workingLayers.head.size
    val persistenceSum = workingLayers.map(_.persist).sum
    workingLayers.foldLeft(Layer.filledWith0(size, 1))(_+_) / math.pow(persistenceSum, octaves)
  }

  /**
   * Smooth the final matrix
   * @author Simon RONCIERE
   * @param size size of the matrix
   * @param smoothParam intensity of the smoothing
   * @param layerSum Layer built with the sum of working layers
   * @return final matrix
   */
  def smooth (size: Int, smoothParam:Int,layerSum:Layer):Layer={
    val smoothedMatrix = Layer.filledWith0(size, 0)
    for (coordonne_x <- 0 until size; coordonne_y <- 0 until size) {
      var SumTiles,NbLayers = 0
      val sizeList = (0 until size).toList
      //for (k <- (math.max(coordonne_x - smoothParam,0)) to (math.min(coordonne_x + smoothParam,size)); l <- (math.max(coordonne_y - smoothParam,0)) to math.min(coordonne_y + smoothParam,0)) {
      for (k <- (coordonne_x - smoothParam) to (coordonne_x + smoothParam); l <- (coordonne_y - smoothParam) to (coordonne_y + smoothParam)) {
        if (sizeList.contains(k) && sizeList.contains(l)) {
          NbLayers = NbLayers + 1
          SumTiles = SumTiles + layerSum.matrix(k)(l)
        }
      }
      smoothedMatrix.matrix(coordonne_x)(coordonne_y) = SumTiles / NbLayers
    }
    smoothedMatrix
  }

  /**
   * Build a layer with Perlin noise
   * @author Simon RONCIERE
   * @param frequency frequency of the regular point
   * @param octaves Number of layers
   * @param persistence value necessary to create a layer
   * @param smoothParam intensity of the smoothing
   * @param size initial size
   * @return the generated layer
   */
  def generateLayer(frequency: Int, octaves: Int, persistence: Float, smoothParam: Int, size: Int): Layer = {
    val workingLayers = generateWorkingLayer(octaves, persistence, size, frequency)
    val sum_layer = sumLayers(workingLayers)
    smooth(size,smoothParam,sum_layer)
  }

  /**
   * Generate an array of working layer
   * @author Thomas GIOVANNINI
   * @param octaves the size of the array
   * @param persistence the layer persistance
   * @return the list of working layer filled with 0s
   */
  def generateWorkingLayer(octaves: Int, persistence: Float, size: Int, frequency: Int): List[Layer] = {
    val randomCalque = Layer.filledWithRandom(size, 1)
    (1 to octaves).map { index =>
      fillWorkingLayers(size, math.pow(persistence, index).toFloat, math.pow(frequency, index).toInt, randomCalque)
    }.toList
  }

  /**
   * Build the matrix of a working layer
   * @author Thomas GIOVANNINI
   * @param size size of the matrix to smooth
   * @param persistence of the layer
   * @param frequency frequency of the regular point
   * @param randomLayer Layer interpolated
   * @return the matrix of a working layer
   */
  def fillWorkingLayers(size: Int, persistence: Float, frequency: Int, randomLayer: Layer): Layer = {
    val matrix = Array.ofDim[Int](size, size)
    for (coordX <- 0 until size; coordY <- 0 until size)/*Keeping for loop for a better understanding*/
      matrix(coordX)(coordY) = interpolateLayer(coordX, coordY, frequency.toInt, randomLayer)
    Layer(matrix, persistence)
  }

  /**
   * Build the bounds of values so interpolate
   * @author Simon RONCIERE
   * @param ratio Coordinate / step
   * @param step size / frequency
   * @param size size of the matrix where function is use
   * @return (lower bound, highter bound)
   */
  def buildBounds(ratio:Int, step: Float,size:Int):(Int,Int)={
    var SupBound = ((ratio + 1) * step).toInt
    if (SupBound >= size) SupBound = size- 1
    ((ratio * step).toInt,SupBound)
  }

  /**
   * Interpolate a point in a layer
   * @author Simon RONCIERE
   * @param xCoordinate coordinate x of the considered point
   * @param yCoordinate coordinate x of the considered point
   * @param frequency frequency of the regular point
   * @param randomLayer Layer interpolated
   * @return value of the point interpolate
   */
  def interpolateLayer(xCoordinate: Int, yCoordinate: Int, frequency: Int, randomLayer: Layer): Int = {

    val step: Float = randomLayer.size.toFloat / frequency.toFloat

    val (infBoundX,supBoundX)=buildBounds((xCoordinate.toFloat / step).toInt,step,randomLayer.size)
    val (infBoundY,supBoundY)=buildBounds((yCoordinate.toFloat / step).toInt,step,randomLayer.size)
    
    /* récupérations des valeurs aléatoires aux bornes */
    val valInfxInfy = randomLayer.matrix(infBoundX)(infBoundY)
    val valInfxSupy = randomLayer.matrix(infBoundX)(supBoundY)
    val valSupxInfy = randomLayer.matrix(supBoundX)(infBoundY)
    val valSupxSupy = randomLayer.matrix(supBoundX)(supBoundY)

    val interpolateInfx = interpolate(valInfxInfy, valInfxSupy, supBoundY - infBoundY, yCoordinate - infBoundY)
    val interpolateSupx = interpolate(valSupxInfy, valSupxSupy, supBoundY - infBoundY, yCoordinate - infBoundY)
    interpolate(interpolateInfx, interpolateSupx, supBoundX - infBoundX, xCoordinate - infBoundX)

  }

  /**
   * Polynomial interpolation of values
   * @author Simon RONCIERE
   * @param val1 first value
   * @param val2 second value
   * @param intervalWidth width of the interval beetween the values
   * @param delta distance to the lower bound
   * @return value interpolate
   */
  private def interpolate(val1: Int, val2: Int, intervalWidth: Int, delta: Int): Int = {
    if (intervalWidth == 0)
      val1
    else if (intervalWidth == 1)
      val2
    else {
      val a = delta / intervalWidth
      val fac1 = 3 * math.pow(1 - a, 2) - 2 * math.pow(1 - a, 3)
      val fac2 = 3 * math.pow(a, 2) - 2 * math.pow(a, 3)
      (val1 * fac1 + val2 * fac2).toInt
    }
  }
}
