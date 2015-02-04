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
    val octave = 100
    val frequence = 30
    val persistance= 0.5f
    val taille_sortie = 200
    val lissage = 3

    val calque = Layer.filledWith0(taille_sortie,1)
    val matrice = Array.ofDim[Int](taille_sortie, taille_sortie)

    Layer.generateLayer(frequence,octave,persistance,lissage,calque)
  }
}
