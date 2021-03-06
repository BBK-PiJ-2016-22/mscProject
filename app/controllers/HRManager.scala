package controllers

import model.matchingLogic.attributeFactory.{Attribute, AttributeFactory, AttributesSorting}
import model.matchingLogic.AlgorithmFactory.AlgorithmFactory
import model.matchingLogic.MatchingMethodsFacade
import model.matchingLogic.candidatesSorting.CandidatesSortedByLockedAttributes
import model.matchingLogic.candidatesSortingFactory.CandidateSortingFactory
import model.persistenceAPIInterface.attributesPersistence.{CompetencyPersistenceFacade, SkillPersistenceFacade}
import model.persistenceAPIInterface.candidateProfilePersistence.CandidatePersistenceFacade
import model.persistenceAPIInterface.jobDescriptionPersistence.{JobDescriptionCompetencyPersistenceFacade, JobDescriptionPersistenceFacade, JobDescriptionSkillPersistenceFacade}
import model.persistenceAPIInterface.messagingPersistence.{ChatMessagePersistenceFacade, UserReceivedMessagePersistenceFacade, UserSentMessagePersistenceFacade}
import model.persistenceAPIInterface.userPersistence.UserPersistenceFacade
import persistenceAPI.DataBaseConnection.objects.JobDescription
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}

/**
  * Controller Methods for HR manager part of the Application.
  */
object HRManager extends Controller {

  /**
    * Action for hrManagerMain
    * Renders hrManagerMain template
    *
    */
  def hrManagerMain = Action {
    implicit request =>
      val userName = request.session.get("username")
      val priv = request.session.get("privilege")

      priv match {
        case None =>
          Redirect("/")
        case Some("HRManager") =>
          Ok(views.html.hrManager.hrManagerMain(userName.get))
        case Some("SuperUser") =>
          Ok(views.html.hrManager.hrManagerMain(userName.get))
        case _ =>
          Redirect("/")
      }

  }

  /**
    * Action for createJobDescription
    * Renders createJobDescription template.
    *
    * @return
    */
  def createJobDescription() = Action {
    implicit request =>
      val priv = request.session.get("privilege")

      priv match {
        case None =>
          Redirect("/")
        case Some("HRManager") | Some("SuperUser") =>
          Ok(views.html.hrManager.createJobDescription())
        case _ =>
          Redirect("/")
      }

  }

  /**
    * Form for post request from create jobDescription form
    */
  val jobDescriptionForm = Form {
    tuple("jobTitle" -> text,
      "educationName" -> text,
      "educationLevel" -> text,
      "experienceLevel" -> text
    )
  }

  /**
    * Action for create jobDescription post form
    *
    * @return
    */
  def jobDescriptionPost() = Action {
    implicit request =>

      val (jobTitle, educationName, educationLevel,
      experienceLevel) = jobDescriptionForm.bindFromRequest().get

      val jobDescriptionPersistence = JobDescriptionPersistenceFacade

      jobDescriptionPersistence.addJobProfile(jobTitle, educationName, educationLevel, experienceLevel)

      Redirect("/hrManagerMain")

  }

  /**
    * Action for rendering the viewJobDescription template
    *
    * @return
    */
  def viewJobDescription() = Action {
    implicit request =>
      val priv = request.session.get("privilege")


      priv match {
        case None =>
          Redirect("/")
        case Some("HRManager") | Some("SuperUser") =>

          val jobDescriptionPersistence = JobDescriptionPersistenceFacade

          val jobDescriptions: List[JobDescription] =
            jobDescriptionPersistence.getAllJobDescriptions()

          Ok(views.html.hrManager.viewJobDescription(jobDescriptions))
        case _ =>
          Redirect("/")
      }

  }

  /**
    * Action for rendering jobDescription template.
    *
    * @return
    */
  def jobDescription() = Action {

    implicit request =>

      val id: Option[String] = request.getQueryString("id")
      val priv = request.session.get("privilege")

      if (id.isDefined) {

        priv match {
          case None =>
            Redirect("/")
          case Some("HRManager") | Some("SuperUser") =>

            val jobDescriptionPersistence = JobDescriptionPersistenceFacade

            Ok(views.html.hrManager.jobDescription(jobDescriptionPersistence
              .getJobProfileByID(id.get.toInt).get))
          case _ =>
            Redirect("/")
        }

      } else {
        Forbidden
      }


  }

  /**
    * Action for rendering the addSkillJobDescription template
    *
    * @return
    */
  def addSkillJobDescription() = Action {

    implicit request =>
      val priv = request.session.get("privilege")
      val jobDescriptionID: Option[String] = request.getQueryString("jobDescriptionID")

      if (jobDescriptionID.isDefined) {

        priv match {
          case None =>
            Redirect("/")
          case Some("HRManager") | Some("SuperUser") =>

            val skillPersistence = SkillPersistenceFacade

            Ok(views.html.hrManager
              .addSkillHRManager(skillPersistence.getAllSkills())(jobDescriptionID.get.toInt))
          case _ =>
            Redirect("/")
        }

      } else {
        Forbidden
      }
  }

  /**
    * Form for post request for addSkillJobDescription
    */
  val skillAddForm = Form {
    tuple("skillID" -> text,
      "rating" -> text,
      "jobDescriptionID" -> text
    )
  }

  /**
    * Action for post request addSkillJobDescription
    *
    * @return
    */
  def addSkillJobDescriptionPost() = Action {

    implicit request =>

      val (skillID, rating, jobDescriptionID) = skillAddForm.bindFromRequest().get

      val jobDescriptionSkillFacade = JobDescriptionSkillPersistenceFacade

      jobDescriptionSkillFacade
        .addJobProfileSkill(skillID.toInt, rating.toInt, jobDescriptionID.toInt)

      Redirect(s"/hrManagerMain/jobDescription?id=$jobDescriptionID")

  }

  /**
    * Action for rendering addCompetencyJobDescription template.
    *
    * @return
    */
  def addCompetencyJobDescription() = Action {

    implicit request =>
      val priv = request.session.get("privilege")
      val jobDescriptionID: Option[String] = request.getQueryString("jobDescriptionID")


      if (jobDescriptionID.isDefined) {

        priv match {
          case None =>
            Redirect("/")
          case Some("HRManager") | Some("SuperUser") =>

            val competencyPersistence = CompetencyPersistenceFacade

            Ok(views.html.hrManager.addCompetencyHRManager
            (competencyPersistence.getAllCompetencies())(jobDescriptionID.get.toInt))
          case _ =>
            Redirect("/")
        }
      } else {
        Forbidden
      }

  }

  /**
    * Form for post request add Competency
    */
  val competencyAddForm = Form {
    tuple("competencyID" -> text,
      "rating" -> text,
      "jobDescriptionID" -> text
    )
  }

  /**
    * Action for addCompetencyJobDescription post form
    *
    * @return
    */
  def addCompetencyJobDescriptionPost() = Action {

    implicit request =>

      val (competencyID, rating, jobDescriptionID) = competencyAddForm.bindFromRequest().get

      val jobDescriptionCompetencyFacade = JobDescriptionCompetencyPersistenceFacade

      jobDescriptionCompetencyFacade.
        addJobProfileCompetency(competencyID.toInt, rating.toInt, jobDescriptionID.toInt)

      Redirect(s"/hrManagerMain/jobDescription?id=$jobDescriptionID")

  }

  /**
    * Action for rendering matchingMain template
    * Method using manual dependency injection
    *
    * @return
    */
  def matchingMain() = Action {
    implicit request =>
      val priv = request.session.get("privilege")
      val jobDescriptionID: Option[String] = request.getQueryString("jobDescriptionID")
      val called: Option[String] = request.getQueryString("called")

      if (jobDescriptionID.isDefined) {

        // get sorted candidates with factory
        val candidateSortingFactory = CandidateSortingFactory

        val candidates = candidateSortingFactory.factory("candidatesSortedByOneSkill")
          .returnCandidatesByJobDescriptionID(jobDescriptionID.get.toInt)

        priv match {
          case None =>
            Redirect("/")
          case Some("HRManager") | Some("SuperUser") =>

            // algorithm factory for dependency injection
            val algFactory = AlgorithmFactory

            called match {

              case None =>
                Ok(views.html.hrManager.matchingMain(Nil)(jobDescriptionID.get.toInt)("yes"))

              case Some("allCapped") =>

                val matching = MatchingMethodsFacade(algFactory
                  .factory("allCappedSimilarityFacade"), candidates)
                val matchingCandidates =
                  matching.getListOfMachingCandidatesFromDB(jobDescriptionID.get.toInt)

                Ok(views.html.hrManager
                  .matchingMain(matchingCandidates)(jobDescriptionID.get.toInt)(called.get))

              case Some("unCapped") =>
                val matching = MatchingMethodsFacade(algFactory
                  .factory("unCappedSimilarityFacade"), candidates)
                val matchingCandidates =
                  matching.getListOfMachingCandidatesFromDB(jobDescriptionID.get.toInt)

                Ok(views.html.hrManager.matchingMain(matchingCandidates)(jobDescriptionID
                  .get.toInt)(called.get))

              case Some("lockIndividually") =>

                // get lists of sorted attributes as Skills and Competencies
                val (skills, competencies) = AttributesSorting.sortAttributes(listOfAttributes)


                // get sorted candidates with without factory
                val candidatesSorted = CandidatesSortedByLockedAttributes()
                candidatesSorted.setAttributes(skills, competencies)

                val candidates =
                  candidatesSorted.returnCandidatesByJobDescriptionID(jobDescriptionID.get.toInt)

                val matching = MatchingMethodsFacade(algFactory.factory("allCappedSimilarityFacade"), candidates)

                val matchingCandidates =
                  matching.getListOfMachingCandidatesFromDB(jobDescriptionID.get.toInt)

                listOfAttributes = Nil

                Ok(views.html.hrManager.matchingMain(matchingCandidates)(jobDescriptionID.get.toInt)(called.get))

            }
          case _ =>
            Redirect("/")
        }
      } else {
        Forbidden
      }
  }

  /**
    * form for sending jobDescriptionID
    */
  val matchingMainForm = Form {
    tuple(
      "jobDescriptionID" -> text,
      "actionType" -> text
    )
  }

  /**
    * Action for matchingMain post request.
    *
    * @return
    */
  def matchingMainPost() = Action {
    implicit request =>

      val (jobDescriptionID, actionType) = matchingMainForm.bindFromRequest().get

      actionType match {
        case "allCapped" | "unCapped" =>
          Redirect(s"/hrManagerMain/" +
            s"jobDescription/matchingMain?jobDescriptionID=$jobDescriptionID&called=$actionType")
        // redirects to lockAttributes
        case "lockIndividually" =>
          Redirect(s"/hrManagerMain/jobDescription/matchingMain/lockAttributes?jobDescriptionID=$jobDescriptionID")
      }

  }


  /**
    * Global state list of attributes
    */
  var listOfAttributes: List[Attribute] = Nil

  def lockAttributes() = Action {

    implicit request =>

      val jobDescriptionID: Option[String] = request.getQueryString("jobDescriptionID")

      // optional
      val skillID: Option[String] = request.getQueryString("skillID")
      val competencyID: Option[String] = request.getQueryString("competencyID")
      val rating: Option[String] = request.getQueryString("rating")

      val jobDescriptionPersistence = JobDescriptionPersistenceFacade


      if (skillID.isDefined) {
        listOfAttributes = listOfAttributes :+
          AttributeFactory.createAttribute("skill")(skillID.get.toInt,
            rating.get.toInt)
      }

      if (competencyID.isDefined) {
        listOfAttributes = listOfAttributes :+
          AttributeFactory.createAttribute("competency")(competencyID.get.toInt, rating.get.toInt)
      }

      Ok(views.html.hrManager.
        lockAttributes(jobDescriptionPersistence.getJobProfileByID(jobDescriptionID.get.toInt)
          .get)(listOfAttributes))
  }

  /**
    * Lock attibutesForm
    */
  val lockAttributesForm = Form {
    "jobDescriptionID" -> text
  }

  /**
    * Post Method for lock attributes.
    *
    * @return
    */
  def lockAttributesPost() = Action {

    implicit request =>

      val jobDescriptionID: Option[String] = request.getQueryString("jobDescriptionID")

      Redirect(s"/hrManagerMain/" +
        s"jobDescription/matchingMain?jobDescriptionID=${jobDescriptionID.get}&called=lockIndividually")

  }


  /**
    * Action for rendering viewMatchingCandidates template.
    *
    * @return Action
    */
  def viewMatchedCandidates() = Action {

    implicit request =>
      val priv = request.session.get("privilege")
      val id: Option[String] = request.getQueryString("id")
      val d1: Option[String] = request.getQueryString("d1")
      val d2: Option[String] = request.getQueryString("d2")
      val d3: Option[String] = request.getQueryString("d3")
      val jobDescriptionID: Option[String] = request.getQueryString("jobDescriptionID")
      val called: Option[String] = request.getQueryString("called")

      if (jobDescriptionID.isDefined && id.isDefined
        && d1.isDefined && d2.isDefined && d3.isDefined) {

        priv match {
          case None =>
            Redirect("/")
          case Some("HRManager") | Some("SuperUser") =>

            val candidatePersistence = CandidatePersistenceFacade

            Ok(views.html.hrManager
              .viewMatchedCandidate(candidatePersistence.getCandidateByID(id.get.toInt).get)
              (d1.get.toDouble)(d2.get.toDouble)(d3.get.toDouble)(jobDescriptionID.get.toInt)(called.get))
          case _ =>
            Redirect("/")
        }
      } else {
        Forbidden
      }
  }

  /**
    * Action for rendering inbox template.
    *
    * @return Action
    */
  def inbox() = Action {

    implicit request =>
      val priv = request.session.get("privilege")
      val userID = request.session.get("userID")

      priv match {
        case None =>
          Redirect("/")
        case Some("HRManager") | Some("SuperUser") =>

          val userReceivedMessagePersistence = UserReceivedMessagePersistenceFacade

          Ok(views.html.hrManager.
            hrManagerInbox(userReceivedMessagePersistence.
              getAllUserReceivedMessageByID(userID.get.toInt))
            (userReceivedMessagePersistence.countUnreadMessagesByUserID(userID.get.toInt)))
        case _ =>
          Redirect("/")
      }
  }

  /**
    * Action for rendering sentInbox.
    *
    * @return Action
    */
  def sentInbox() = Action {

    implicit request =>
      val priv = request.session.get("privilege")
      val userID = request.session.get("userID")

      priv match {
        case None =>
          Redirect("/")
        case Some("HRManager") | Some("SuperUser") =>

          val userSentMessagePersistence = UserSentMessagePersistenceFacade

          val userReceivedMessagePersistence = UserReceivedMessagePersistenceFacade

          Ok(views.html.hrManager.
            hrManagerSentInbox(userSentMessagePersistence
              .getAllUserSentMessageByID(userID.get.toInt))
            (userReceivedMessagePersistence.countUnreadMessagesByUserID(userID.get.toInt)))
        case _ =>
          Redirect("/")
      }


  }

  /**
    * Action for rendering readMessage.
    *
    * @return Action
    */
  def readMessage() = Action {

    implicit request =>
      val priv = request.session.get("privilege")
      val chatMessageID: Option[String] = request.getQueryString("ChatMessageID")
      val userID = request.session.get("userID")

      if (chatMessageID.isDefined) {
        priv match {
          case None =>
            Redirect("/")
          case Some("HRManager") | Some("SuperUser") =>

            val chatMessagePersistence = ChatMessagePersistenceFacade

            chatMessagePersistence.setReadToTrueByID(chatMessageID.get.toInt)

            val userReceivedMessagePersistence = UserReceivedMessagePersistenceFacade


            Ok(views.html.hrManager.
              hrManagerReadMessage
              (chatMessagePersistence.getChatMessageByID(chatMessageID.get.toInt).get)
              (userReceivedMessagePersistence.countUnreadMessagesByUserID(userID.get.toInt)))

          case _ =>
            Redirect("/")
        }
      } else {
        Forbidden
      }

  }

  /**
    * Action for rendering sendMessage.
    *
    * @return Action
    */
  def sendMessage() = Action {

    implicit request =>
      val priv = request.session.get("privilege")

      priv match {
        case None =>
          Redirect("/")
        case Some("HRManager") | Some("SuperUser") =>

          val userPersistence = UserPersistenceFacade

          Ok(views.html.hrManager.hrManagerSendMessage(userPersistence.getAllRecruiterUsers()))


        case _ =>
          Redirect("/")
      }

  }

  val sendMessageForm = Form {
    tuple(
      "subject" -> text,
      "messageBody" -> text,
      "receiverID" -> text
    )
  }

  /**
    * Action for sendMessagePost.
    *
    * @return Action
    */
  def sendMessagePost() = Action {

    implicit request =>

      val senderUserID = request.session.get("userID").get

      val (subject, messageBody, receiverID) = sendMessageForm.bindFromRequest().get

      val chatMessagePersistence = ChatMessagePersistenceFacade

      chatMessagePersistence.createSentNewChatMessage(senderUserID.toInt, receiverID.toInt, subject, messageBody)

      Redirect("inbox")
  }

  /**
    * Action for rendering delete candidate.
    *
    * @return Action.
    */
  def deleteJobDescription = Action {

    implicit request =>
      val priv = request.session.get("privilege")

      priv match {
        case None =>
          Redirect("/")
        case Some("HRManager") | Some("SuperUser") =>

          val jobDescriptionPersistence = JobDescriptionPersistenceFacade

          Ok(views.html.hrManager.deleteJobDescription(jobDescriptionPersistence.getAllJobDescriptions()))

        case _ =>
          Redirect("/")
      }


  }

  /**
    * DeleteJobDecriptionForm.
    */
  val deleteJobDescriptionForm = Form {
    "jobDescriptionID" -> text
  }

  /**
    * Action for deleteJobDescriptionPost.
    *
    * @return Action
    */
  def deleteJobDescriptionPost = Action {

    implicit request =>

      val jobDescriptionID = deleteJobDescriptionForm.bindFromRequest().get

      if (jobDescriptionID != "#") {

        val jobDescriptionPersistence = JobDescriptionPersistenceFacade

        jobDescriptionPersistence.deleteJobDescription(jobDescriptionID.toInt)
      }

      Redirect("/hrManagerMain/deleteJobDescription")

  }

}
