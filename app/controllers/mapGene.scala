package controllers

import models.map.Calque
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html

/**
 * Test controller for the map generation
 */

object mapGene extends Controller{

  def index = Action{
    val output = Html(main())
    Ok(output)
  }
 /* private def  main2( ): String = {
    val tableau = new Array[Int](20)
    val random = scala.util.Random
    val frequence =5
    var r="random = "

    for(i<-0 until 20){
        tableau(i) = random.nextInt(20)
      r=r+tableau(i).toString+" | "
    }
    r=r+"\n\n inter = "
    for (i<-0 until 20){
      tableau(i)=valeur_interpolee(i,frequence,tableau)
      r=r+tableau(i).toString+" | "
    }
    r=r+"\n\n"
    r
  }
  def affichetab(tableau:Array[Int]):String = {
    var r=""
    for (i<-0 until tableau.length){
      r = r+tableau(i)
    }
    r
  }
  def valeur_interpolee(i: Int, frequence: Int, r: Array[Int]): Int = {
    /* déterminations des bornes */
    val pas = r.length.toFloat / frequence.toFloat

    var q: Int = (i.toFloat / pas).toInt
    val borne1x: Int = (q * pas).toInt
    var borne2x: Int = ((q + 1) * pas).toInt

    if (borne2x >= r.length)
      borne2x = r.length - 1

    /* récupérations des valeurs aléatoires aux bornes */
    val b0 = r(borne1x)
    val b1 = r(borne2x)

    val v1 = interpolate(b0, b1, b1 - b0, i - b0)

    v1
  }
  private def interpolate(y1: Int, y2: Int, n: Int, delta: Int): Int = {
    // interpolation non linéaire
    if (n == 0)
      return y1
    if (n == 1)
      return y2

    val a = delta / n

    val fac1 = 3 * math.pow(1 - a, 2) - 2 * math.pow(1 - a, 3)
    val fac2 = 3 * math.pow(a, 2) - 2 * math.pow(a, 3)

    (y1 * fac1 + y2 * fac2).toInt

  }*/

  private def  main( ): String = {
    /* déterminations des bornes */
    val octave = 100
    val frequence = 30
    val persistance= 0.5f
    val taille_sortie = 200
    val lissage = 3

    val calque = Calque.filledWith0(taille_sortie,1)
    val matrice = Array.ofDim[Int](taille_sortie, taille_sortie)

    Calque.generer_calque(frequence,octave,persistance,lissage,calque)

    //calque.toString()

/*    val r = scala.util.Random
    for (i <- 0 until taille_sortie; j <- 0 until taille_sortie)
      matrice(i)(j) =r.nextInt(256)


    var res=""
    for (x <- 0 until taille_sortie){
      for (y <- 0 until taille_sortie){
        res=res + matrice(x)(y).toString +" | "
      }
      res=res + "\n"
    }
    res*/
  }
}
