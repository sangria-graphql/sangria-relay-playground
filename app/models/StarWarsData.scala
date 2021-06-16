package models

import java.util.concurrent.atomic.AtomicInteger
import sangria.relay.{Identifiable, Node}

object StarWarsData {

  case class Ship(id: String, name: String) extends Node

  case class Faction(id: String, name: String, ships: List[String])

  object Faction {

    implicit object FactionIdentifiable extends Identifiable[Faction] {
      def id(faction: Faction): String = faction.id
    }

  }

  object Ships {
    val xwing: Ship = Ship("1", "X-Wing")
    val ywing: Ship = Ship("2", "Y-Wing")
    val awing: Ship = Ship("3", "A-Wing")
    val falcon: Ship = Ship("4", "Millenium Falcon")
    val homeOne: Ship = Ship("5", "Home One")
    val tieFighter: Ship = Ship("6", "TIE Fighter")
    val tieInterceptor: Ship = Ship("7", "TIE Interceptor")
    val executor: Ship = Ship("8", "Executor")

    val All: Seq[Ship] = xwing :: ywing :: awing :: falcon :: homeOne :: tieFighter :: tieInterceptor :: executor :: Nil
  }

  object Factions {
    val rebels: Faction = Faction("1", "Alliance to Restore the Republic", List("1", "2", "3", "4", "5"))
    val empire: Faction = Faction("2", "Galactic Empire", List("6", "7", "8"))

    val All: Seq[Faction] = rebels :: empire :: Nil
  }

  class FactionRepo {
    val nextShipId = new AtomicInteger(9)

    var ships: Seq[Ship] = Ships.All
    var factions: Seq[Faction] = Factions.All

    def createShip(shipName: String, factionId: String): Ship = {
      val newShip = Ship("" + nextShipId.getAndIncrement(), shipName)

      ships = ships :+ newShip
      factions = factions.map {
        case f if f.id == factionId => f.copy(ships = f.ships :+ newShip.id)
        case f => f
      }
      newShip
    }

    def getShip(id: String): Option[Ship] = ships find (_.id == id)

    def getFaction(id: String): Option[Faction] = factions find (_.id == id)

    def getFactions(names: Seq[String]): Seq[Option[Faction]] = {
      names.map(getFractionFromName)
    }

    def getFractionFromName(name: String): Option[Faction] = {
      if (name == "empire")
        getEmpire
      else if (name == "rebels")
        getRebels
      else
        None
    }

    def getRebels: Option[Faction] = factions find (_.id == "1")

    def getEmpire: Option[Faction] = factions find (_.id == "2")
  }

}
