package controllers

import model.persistenceAPIInterface.attributesPersistence.{CompetencyPersistenceFacade, SkillPersistenceFacade}
import model.persistenceAPIInterface.userPersistence.UserPersistenceFacade
import play.api.data.Form
import play.api.mvc.{Action, Controller}
import play.api.data.Forms._

/**
  * Controller methods for superUser part of the application.
  */
object SuperUser extends Controller {

  /**
    * Action for rendering superUserMain template
    *
    * @return
    */
  def superUserMain = Action {
    implicit request =>

      val userName = request.session.get("username")
      val priv = request.session.get("privilege")

      priv match {
        case None =>
          Redirect("/")
        case Some("SuperUser") =>
          Ok(views.html.superUser.superUserMain())
        case _ =>
          Redirect("/")
      }

  }

  /**
    * Action for rendering addSkill
    *
    * @return
    */
  def addSkill = Action {
    implicit request =>
      val priv = request.session.get("privilege")

      val skillPersistence = SkillPersistenceFacade

      priv match {
        case None =>
          Redirect("/")
        case Some("SuperUser") =>
          Ok(views.html.superUser.addSkill(skillPersistence.getAllSkills()))
        case _ =>
          Redirect("/")
      }

  }

  /**
    * Form for addSkill post request
    */
  val skillForm = Form(
    "name" -> text
  )

  /**
    * post request for addSkill
    *
    * @return
    */
  def submitSkill = Action {

    implicit request =>

      val name = skillForm.bindFromRequest().get

      val skillPersistence = SkillPersistenceFacade

      skillPersistence.addSkill(name)

      Redirect("/superUserMain/addSkill")

  }

  /**
    * Form for addCompetency request
    *
    */
  val competencyForm = Form(
    "name" -> text
  )

  /**
    * Action for rendering addCompetency template
    *
    * @return
    */
  def addCompetency = Action {
    implicit request =>
      val priv = request.session.get("privilege")

      val competencyPersistence = CompetencyPersistenceFacade


      priv match {
        case None =>
          Redirect("/")
        case Some("SuperUser") =>
          Ok(views.html.superUser.addCompetency(competencyPersistence.getAllCompetencies()))
        case _ =>
          Redirect("/")
      }

  }

  /**
    * Form request for addCompetency.
    *
    * @return
    */
  def submitCompetency = Action {

    implicit request =>

      val name = competencyForm.bindFromRequest().get

      val competencyPersistence = CompetencyPersistenceFacade

      competencyPersistence.addCompetency(name)

      Redirect("/superUserMain/addCompetency")

  }

  /**
    * Action for rendering addUser
    *
    * @return
    */
  def addUser = Action {
    Ok(views.html.superUser.addUser())
  }

  val addUserForm = Form(
    tuple(
      "userName" -> text,
      "password" -> text,
      "privilege" -> text
    )
  )

  /**
    * Form request for addUser
    *
    * @return
    */
  def addUserPost = Action {

    implicit request =>

      val (userName, password, privilege) = addUserForm.bindFromRequest().get

      val persistenceFacade = UserPersistenceFacade

      val privilegeString = PrivilegeStringFactory.getPrivilege(privilege.toInt)

      persistenceFacade.insertNewUser(userName, password, privilegeString)

      Redirect("/superUserMain")

  }

  /**
    * Rendering delete skill.
    *
    * @return
    */
  def deleteSkill = Action {

    implicit request =>
      val priv = request.session.get("privilege")

      val skillPersistence = SkillPersistenceFacade

      priv match {
        case None =>
          Redirect("/")
        case Some("SuperUser") =>
          Ok(views.html.superUser.deleteSkill(skillPersistence.getAllSkills()))
        case _ =>
          Redirect("/")
      }

  }

  /**
    * form for deleting skill
    */
  val deleteSkillForm = Form(
    "skillID" -> text
  )

  /**
    * Delete skill post.
    *
    * @return Action
    */
  def deleteSkillPost = Action {

    implicit request =>

      val skillID = deleteSkillForm.bindFromRequest().get

      if (skillID != "#") {
        val skillPersistence = SkillPersistenceFacade
        skillPersistence.deleteSkill(skillID.toInt)

      }

      Redirect("/superUserMain/deleteSkill")

  }

  def deleteCompetency = Action {

    implicit request =>
      val priv = request.session.get("privilege")

      val competencyPersistence = CompetencyPersistenceFacade

      priv match {
        case None =>
          Redirect("/")
        case Some("SuperUser") =>
          Ok(views.html.superUser.deleteCompetency(competencyPersistence.getAllCompetencies()))
        case _ =>
          Redirect("/")
      }

  }

  /**
    * form for deleting skill
    */
  val deleteCompetencyForm = Form(
    "competencyID" -> text
  )

  /**
    * Delete skill post.
    *
    * @return Action
    */
  def deleteCompetencyPost = Action {

    implicit request =>

      val competencyID = deleteCompetencyForm.bindFromRequest().get

      if (competencyID != "#") {
        val competencyPersistence = CompetencyPersistenceFacade
        competencyPersistence.deleteCompetency(competencyID.toInt)
      }

      Redirect("/superUserMain/deleteCompetency")

  }
}
