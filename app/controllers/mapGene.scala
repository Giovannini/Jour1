package controllers

import models.map.Layer
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

  private def  main( ): String = {
    /* déterminations des bornes */
    val octave = 35
    val frequence = 20
    val persistance= 0.5f
    val taille_sortie = 150
    val lissage = 3

    val calque = Layer.filledWith0(taille_sortie,1)
    val matrice = Array.ofDim[Int](taille_sortie, taille_sortie)

    Layer.generateLayer(frequence,octave,persistance,lissage,calque)

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
