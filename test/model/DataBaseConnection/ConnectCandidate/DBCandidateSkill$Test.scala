package model.DataBaseConnection.ConnectCandidate

import model.DataBaseConnection.DBMain
import org.scalatest.FunSuite

/**
  * Created by Casper on 10/07/2017.
  */
class DBCandidateSkill$Test extends FunSuite {

  // CandidateSkill Tests

  test("addCandidateSkill"){

    val db = DBCandidateSkill

    db.addCandidateSkill(1, 5, 1)

    //val competency = db.getSkillByID(1)

    //assert(competency(1) == "Leadership")

  }

}