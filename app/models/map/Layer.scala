package models.map

/**
 * Model class for layer
 */
case class Layer(size: Int, persist: Float) {

  private val matrix = Array.ofDim[Int](size, size)

  /**
   * Display the layer matrix
   * @author Thomas GIOVANNINI
   * @return a string ready to be displayed
   */
  private def printMatrix(): String = {
    matrix.map(printLine)
      .mkString("\n")
  }

  /**
   * Display a line of an array
   * @author Thomas GIOVANNINI
   * @param arrayLine the line to display
   * @return a string ready to display
   */
  def printLine(arrayLine: Array[Int]): String = {
    arrayLine.map(prettify).mkString("")
  }

  private def prettify(number: Int): String = {
    "<span style=\"width=5px;height=5px;"+{if(number<100) "background-color:blue" else "background-color:rgb("+(255-number)+","+(255-number)+","+(255-number)+")"}+"\">"+"" + "0" +"</span>"
  }

  override def toString: String = printMatrix
}

object Layer {
  /**
   * Fill the matrix with 0
   * @author Simon Ronciere
   * @return the filled matrix
   */
  def filledWith0(size: Int, persistence: Float): Layer = {
    val layer = Layer(size, persistence)
    for (i <- 0 until layer.size; j <- 0 until layer.size)
      layer.matrix(i)(j) = 0
    layer
  }

  /**
   * Fill the matrix of Int with random number
   * @author Simon Ronciere
   * @return the filled matrix
   */
  def filledWithRandom(size: Int, persistence: Float): Layer = {
    val random = scala.util.Random
    val layer = Layer(size, persistence)
    for (i <- 0 until size; j <- 0 until size)
      layer.matrix(i)(j) = random.nextInt(256)
    layer
  }

  def generateLayer(frequency: Int, octaves: Int, persistence: Float, liss: Int, layer: Layer): String = {
    val size = layer.size

    // Generate a random layer
    val randomLayer = Layer.filledWithRandom(size, 1)

    def smooth(size: Int, layer: Layer, strength: Int): Layer = {
      for (coordX <- 0 until size; coordY <- 0 until size) {
        layer.matrix(coordX)(coordY) =
          valeur_interpolee(coordX, coordY, math.pow(frequency, strength).toInt, randomLayer)
      }
      layer
    }

    val calquesTravail = generateWorkingLayer(octaves, persistence, size)
      .zipWithIndex
      .map{ indexedCalque => smooth(size, indexedCalque._1, indexedCalque._2)}

    //var res = calquesTravail.mkString("\n")

    val sum_persistances = calquesTravail.map(_.persist).sum

    //ajout des calques successifs
    for (i <- 0 until size) {
      for (j <- 0 until size) {
        for (n <- 0 until octaves) {
          // ! claque param
          layer.matrix(i)(j) += (calquesTravail(n).matrix(i)(j) * calquesTravail(n).persist).toInt
          //normalisation
          layer.matrix(i)(j) = (layer.matrix(i)(j).toFloat / sum_persistances).toInt
        }
      }

      //Lissage
      /*Il est possible de carrément optimiser cette quadruple boucle en utilisant des map scala.
      Il est pour cela nécessaire de comprendre réellement ce qu'elle fait.
      BoyScoot Thomas*/
      val lissage = Layer.filledWith0(size, 0)
      val sizeList = (0 until size).toList
      for (x <- sizeList; y <- sizeList) {
        var a = 0
        var n = 0
        for (k <- (x - liss) to (x + liss); l <- (y - liss) to (y + liss)) {
          if (sizeList.contains(k) && sizeList.contains(l)) {
            n += 1
            a += layer.matrix(k)(l)
          }
        }
        lissage.matrix(x)(y) = a / n
      }
    }
    val res=layer.toString
    res
  }

  /**
   * Generate an array of working layer
   * @author Thomas GIOVANNINI
   * @param octaves the size of the array
   * @param persistence the layer persistance
   * @return the array of working layer filled with 0s
   */
  def generateWorkingLayer(octaves: Int, persistence: Float, taille: Int): List[Layer] = {
    new Array[Layer](octaves).zipWithIndex
      .map{
        l => Layer.filledWith0(taille, math.pow(persistence, l._2).toFloat)
      }.toList
  }

  def valeur_interpolee(xCoordinate: Int, yCoordinate: Int, frequence: Int, randomLayer: Layer): Int = {
    /* déterminations des bornes */
    //Détermination d'un pas en fonction de la fréquence
    val pas: Float = randomLayer.size.toFloat / frequence.toFloat

    val qX: Int = (xCoordinate.toFloat / pas).toInt
    val borneInfX = (qX * pas).toInt
    val borneSupX = math.min(((qX + 1) * pas).toInt, randomLayer.size - 1)

    val qY = (yCoordinate.toFloat / pas).toInt
    val borneInfY = (qY * pas).toInt
    val borneSupY = math.min(((qY + 1) * pas).toInt, randomLayer.size - 1)

    /* récupérations des valeurs aléatoires aux bornes */
    val xInfyInfBorn = randomLayer.matrix(borneInfX)(borneInfY)
    val xInfySupBorn = randomLayer.matrix(borneInfX)(borneSupY)
    val xSupyInfBorn = randomLayer.matrix(borneSupX)(borneInfY)
    val xSupySupBorn = randomLayer.matrix(borneSupX)(borneSupY)

    val v1 = interpolate(xInfyInfBorn, xInfySupBorn, borneSupY - borneInfY, yCoordinate - borneInfY)
    val v2 = interpolate(xSupyInfBorn, xSupySupBorn, borneSupY - borneInfY, yCoordinate - borneInfY)

    interpolate(v1, v2, borneSupX - borneInfX, xCoordinate - borneInfX)
  }

  /**
   * Fill a matrix of tile with empty tiles
   * @author Simon RONCIERE
   * @return the initialized matrix
   */
  private def interpolate(lowerBorn: Int, upperBorn: Int, intervaleSize: Int, delta: Int): Int = {
    // interpolation non linéaire
    if (intervaleSize == 0) lowerBorn
    else if (intervaleSize == 1) upperBorn
    else {
      val a = delta.toFloat / intervaleSize
      val fac1 = 3 * math.pow(1 - a, 2) - 2 * math.pow(1 - a, 3)
      val fac2 = 3 * math.pow(a, 2) - 2 * math.pow(a, 3)

      (lowerBorn * fac1 + upperBorn * fac2).toInt
    }

  }
}
