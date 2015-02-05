package models.map

/**
 * Model class for layer
 */
case class Layer(size: Int, persist: Float) {

  private val matrix = Array.ofDim[Int](size, size)

  /**
   * Display a matrix
   * @author Thomas Giovannini, Simon Ronciere
   * @return The matrix in a string
   */
  private def printMatrix(): String = {
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
  override def toString: String = {
    //"Calque: size -> " + taille + "; persist = " + persist + "\n" +
    printMatrix
  }
}

object Layer {
  //last boy scoot Thomas GIOVANNINI
  /**
   * Fill the matrix with 0
   * @author Simon Ronciere
   * @param size size of the matrix to fill
   * @param persistence value necessary to create a layer
   * @return the filled matrix
   */
  def filledWith0(size: Int, persistence: Float): Layer = {
    val layer = Layer(size, persistence)
    for (i <- 0 until layer.size; j <- 0 until layer.size)
      layer.matrix(i)(j) = 0
    layer
  }

  //last boy scoot Thomas GIOVANNINI
  /**
   * Fill the matrix of Int with random number
   * @author Simon Ronciere
   * @param size size of the matrix to fill
   * @param persistence value necessary to create a layer
   * @return the filled matrix
   */
  def filledWithRandom(size: Int, persistence: Float): Layer = {
    val random = scala.util.Random
    val layer = Layer(size, persistence)
    for (i <- 0 until size; j <- 0 until size)
      layer.matrix(i)(j) = random.nextInt(256)
    layer
  }

  /**
   * Build the matrix of a working layer
   * @author Thomas Giovannini
   * @param size size of the matrix to smooth
   * @param layer to smooth
   * @param strength intensity of the layer
   * @param frequency frequency of the regular point
   * @param randomLayer Layer interpolated
   * @return the matrix of a working layer
   */

  def fillWorkingLayers(size: Int, layer: Layer, strength: Int, frequency: Int, randomLayer: Layer): Layer = {
    for (coordX <- 0 until size; coordY <- 0 until size) {
      layer.matrix(coordX)(coordY) =
        interpolateLayer(coordX, coordY, math.pow(frequency, strength).toInt, randomLayer)
    }
    layer
  }

  /**
   * Sum works layers according to the persitence of each layer
   * @author Simon Roncière
   * @param size Size of layers
   * @param octaves Number of layers
   * @param persistencesSum sum of persistences
   * @param layer layer completed by the function
   * @param workinglayers list of working layer
   * @return a layer build with working layers
   */
  def sumLayers(size: Int, octaves : Int, persistencesSum:Float, layer:Layer, workinglayers:List[Layer]):Layer={
    for (i <- 0 until size;j <- 0 until size;n <- 0 until octaves) {
      // somme en fonction du paramètre persist qui sert à pondérer les calques
      layer.matrix(i)(j) += (workinglayers(n).matrix(i)(j) * workinglayers(n).persist).toInt
      //normalisation
      layer.matrix(i)(j) = (layer.matrix(i)(j).toFloat / persistencesSum).toInt
    }
    layer
  }

  /**
   * Smooth the final matrix
   * @author Simon Roncière
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
   * Build a layer with a Perlin noise
   * @author Simon Roncière
   * @param frequency frequency of the regular point
   * @param octaves Number of layers
   * @param persistence value necessary to create a layer
   * @param smoothParam intensity of the smoothing
   * @param layer initial Layer 
   * @return A generated matrix in a string
   */
  def generateLayer(frequency: Int, octaves: Int, persistence: Float, smoothParam: Int, layer: Layer): String = {
    val size = layer.size
    // Generate a random layer
    val randomCalque = Layer.filledWithRandom(size, 1)
    // Generate workingLayer, they have different weighting
    val calquesTravail = generateWorkingLayer(octaves, persistence, size)
      .zipWithIndex
      .map{ indexedCalque => fillWorkingLayers(size, indexedCalque._1, indexedCalque._2,frequency,randomCalque)}
    val sum_persistence = calquesTravail.map(_.persist).sum
    val sum_layer = sumLayers(size,octaves,sum_persistence,layer, calquesTravail)
    //smoothing of the final matrix
    val smoothedMatrix = smooth(size,smoothParam,sum_layer)
    smoothedMatrix.toString
  }

  /**
   * Generate an array of working layer
   * @author Thomas GIOVANNINI
   * @param octaves the size of the array
   * @param persistance the layer persistance
   * @return the array of working layer filled with 0s
   */
  def generateWorkingLayer(octaves: Int, persistance: Float, taille: Int): List[Layer] = {
    val calquesTravail = new Array[Layer](octaves)
    for (i <- 0 until octaves) {
      val filledWith0Layer = Layer.filledWith0(taille, math.pow(persistance, i+1).toFloat)
      calquesTravail(i) = filledWith0Layer
    }
    calquesTravail.toList
  }

  /**
   * Build the bounds of values so interpolate
   * @author Simon Roncière
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
   * @author Simon Roncière
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
   * polynomiale interpolation of values
   * @param val1 first value
   * @param val2 second value
   * @param intervalWidth width of the interval beetween the values
   * @param delta distance to the lower bound
   * @return value interpolate
   */
  private def interpolate(val1: Int, val2: Int, intervalWidth: Int, delta: Int): Int = {
    if (intervalWidth == 0)
      return val1
    if (intervalWidth == 1)
      return val2
    val a = delta / intervalWidth
    val fac1 = 3 * math.pow(1 - a, 2) - 2 * math.pow(1 - a, 3)
    val fac2 = 3 * math.pow(a, 2) - 2 * math.pow(a, 3)
    (val1 * fac1 + val2 * fac2).toInt
  }
}
