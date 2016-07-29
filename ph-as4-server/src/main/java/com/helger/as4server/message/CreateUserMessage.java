package com.helger.as4server.message;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.helger.as4lib.ebms3header.Ebms3AgreementRef;
import com.helger.as4lib.ebms3header.Ebms3CollaborationInfo;
import com.helger.as4lib.ebms3header.Ebms3From;
import com.helger.as4lib.ebms3header.Ebms3MessageInfo;
import com.helger.as4lib.ebms3header.Ebms3MessageProperties;
import com.helger.as4lib.ebms3header.Ebms3Messaging;
import com.helger.as4lib.ebms3header.Ebms3PartInfo;
import com.helger.as4lib.ebms3header.Ebms3PartyId;
import com.helger.as4lib.ebms3header.Ebms3PartyInfo;
import com.helger.as4lib.ebms3header.Ebms3PayloadInfo;
import com.helger.as4lib.ebms3header.Ebms3Property;
import com.helger.as4lib.ebms3header.Ebms3Service;
import com.helger.as4lib.ebms3header.Ebms3To;
import com.helger.as4lib.ebms3header.Ebms3UserMessage;
import com.helger.as4lib.marshaller.Ebms3WriterBuilder;
import com.helger.as4lib.soap11.Soap11Body;
import com.helger.as4lib.soap11.Soap11Envelope;
import com.helger.as4lib.soap11.Soap11Header;
import com.helger.commons.collection.ext.CommonsArrayList;
import com.helger.commons.collection.ext.ICommonsList;

/**
 * With the help of this class an usermessage or parts of it can be created.
 *
 * @author bayerlma
 */
public class CreateUserMessage
{

  // TODO Payload as SOAP Body only supported
  public Document createUserMessage (@Nonnull final Ebms3MessageInfo aMessageInfo,
                                     @Nonnull final Ebms3PayloadInfo aEbms3PayloadInfo,
                                     @Nonnull final Ebms3CollaborationInfo aEbms3CollaborationInfo,
                                     @Nonnull final Ebms3PartyInfo aEbms3PartyInfo,
                                     @Nonnull final Ebms3MessageProperties aEbms3MessageProperties,
                                     @Nullable final String sPayloadPath) throws SAXException,
                                                                          IOException,
                                                                          ParserConfigurationException
  {
    // Creating SOAP
    final Soap11Envelope aSoapEnv = new Soap11Envelope ();
    aSoapEnv.setHeader (new Soap11Header ());
    aSoapEnv.setBody (new Soap11Body ());

    // Creating Message
    final Ebms3Messaging aMessage = new Ebms3Messaging ();
    // TODO Needs to beset to 0 (equals false) since holodeck currently throws
    // a exception he does not understand mustUnderstand
    aMessage.setS11MustUnderstand (Boolean.FALSE);
    final Ebms3UserMessage aUserMessage = new Ebms3UserMessage ();

    // Party Information
    aUserMessage.setPartyInfo (aEbms3PartyInfo);

    // Collabration Information
    aUserMessage.setCollaborationInfo (aEbms3CollaborationInfo);

    aUserMessage.setMessageProperties (aEbms3MessageProperties);

    // Payload Information
    aUserMessage.setPayloadInfo (aEbms3PayloadInfo);

    // Message Info
    aUserMessage.setMessageInfo (aMessageInfo);

    aMessage.addUserMessage (aUserMessage);

    // Adding the user message to the existing soap
    final Document aEbms3Message = Ebms3WriterBuilder.ebms3Messaging ().getAsDocument (aMessage);
    aSoapEnv.getHeader ().addAny (aEbms3Message.getDocumentElement ());
    aSoapEnv.getBody ().addAny (MessageHelperMethods.getSoapEnvelope11ForTest (sPayloadPath).getDocumentElement ());
    return Ebms3WriterBuilder.soap11 ().getAsDocument (aSoapEnv);
  }

  public Ebms3PartyInfo createEbms3PartyInfo (final String sFromRole,
                                              final String sFromPartyID,
                                              final String sToRole,
                                              final String sToPartyID)
  {
    final Ebms3PartyInfo aEbms3PartyInfo = new Ebms3PartyInfo ();

    // From => Sender
    final Ebms3From aEbms3From = new Ebms3From ();
    aEbms3From.setRole (sFromRole);
    ICommonsList <Ebms3PartyId> aEbms3PartyIdList = new CommonsArrayList<> ();
    Ebms3PartyId aEbms3PartyId = new Ebms3PartyId ();
    aEbms3PartyId.setValue (sFromPartyID);
    aEbms3PartyIdList.add (aEbms3PartyId);
    aEbms3From.setPartyId (aEbms3PartyIdList);
    aEbms3PartyInfo.setFrom (aEbms3From);

    // To => Receiver
    final Ebms3To aEbms3To = new Ebms3To ();
    aEbms3To.setRole (sToRole);
    aEbms3PartyIdList = new CommonsArrayList<> ();
    aEbms3PartyId = new Ebms3PartyId ();
    aEbms3PartyId.setValue (sToPartyID);
    aEbms3PartyIdList.add (aEbms3PartyId);
    aEbms3To.setPartyId (aEbms3PartyIdList);
    aEbms3PartyInfo.setTo (aEbms3To);
    return aEbms3PartyInfo;
  }

  public Ebms3CollaborationInfo createEbms3CollaborationInfo (final String sAction,
                                                              final String sServiceType,
                                                              final String sServiceValue,
                                                              final String sConversationID,
                                                              final String sAgreementRefPMode,
                                                              final String sAgreementRefValue)
  {
    final Ebms3CollaborationInfo aEbms3CollaborationInfo = new Ebms3CollaborationInfo ();
    aEbms3CollaborationInfo.setAction (sAction);
    final Ebms3Service aEbms3Service = new Ebms3Service ();
    aEbms3Service.setType (sServiceType);
    aEbms3Service.setValue (sServiceValue);
    aEbms3CollaborationInfo.setService (aEbms3Service);
    aEbms3CollaborationInfo.setConversationId (sConversationID);
    final Ebms3AgreementRef aEbms3AgreementRef = new Ebms3AgreementRef ();
    aEbms3AgreementRef.setPmode (sAgreementRefPMode);
    aEbms3AgreementRef.setValue (sAgreementRefValue);
    aEbms3CollaborationInfo.setAgreementRef (aEbms3AgreementRef);
    return aEbms3CollaborationInfo;
  }

  public Ebms3MessageProperties createEbms3MessageProperties (final ICommonsList <Ebms3Property> aEbms3Properties)
  {
    final Ebms3MessageProperties aEbms3MessageProperties = new Ebms3MessageProperties ();
    aEbms3MessageProperties.setProperty (aEbms3Properties);
    return aEbms3MessageProperties;
  }

  public Ebms3PayloadInfo createEbms3PayloadInfo ()
  {
    final Ebms3PayloadInfo aEbms3PayloadInfo = new Ebms3PayloadInfo ();
    aEbms3PayloadInfo.setPartInfo (new CommonsArrayList<> (new Ebms3PartInfo ()));
    return aEbms3PayloadInfo;
  }

  public Ebms3MessageInfo createEbms3MessageInfo (final String sMessageId)
  {
    return MessageHelperMethods.createEbms3MessageInfo (sMessageId, null);
  }

}