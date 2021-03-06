/**
 * Copyright (C) 2015-2017 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.as4.CEF;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.helger.as4.AS4TestConstants;
import com.helger.as4.CAS4;
import com.helger.as4.attachment.WSS4JAttachment;
import com.helger.as4.crypto.AS4CryptoFactory;
import com.helger.as4.crypto.ECryptoAlgorithmSign;
import com.helger.as4.crypto.ECryptoAlgorithmSignDigest;
import com.helger.as4.esens.ESENSPMode;
import com.helger.as4.messaging.domain.AS4UserMessage;
import com.helger.as4.messaging.domain.MessageHelperMethods;
import com.helger.as4.messaging.domain.UserMessageCreator;
import com.helger.as4.messaging.sign.SignedMessageCreator;
import com.helger.as4.mock.MockEbmsHelper;
import com.helger.as4.model.pmode.IPModeIDProvider;
import com.helger.as4.model.pmode.PMode;
import com.helger.as4.server.MockPModeGenerator;
import com.helger.as4.server.message.AbstractUserMessageTestSetUp;
import com.helger.as4.server.standalone.RunInJettyAS4TEST9090;
import com.helger.as4.soap.ESOAPVersion;
import com.helger.as4.util.AS4ResourceManager;
import com.helger.as4lib.ebms3header.Ebms3CollaborationInfo;
import com.helger.as4lib.ebms3header.Ebms3MessageInfo;
import com.helger.as4lib.ebms3header.Ebms3MessageProperties;
import com.helger.as4lib.ebms3header.Ebms3PartyInfo;
import com.helger.as4lib.ebms3header.Ebms3PayloadInfo;
import com.helger.as4lib.ebms3header.Ebms3Property;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.photon.core.servlet.WebAppListener;
import com.helger.xml.serialize.read.DOMReader;

public abstract class AbstractCEFTwoWayTestSetUp extends AbstractUserMessageTestSetUp
{
  protected PMode m_aESENSTwoWayPMode;
  protected ESOAPVersion m_eSOAPVersion;
  protected Node m_aPayload;

  protected AbstractCEFTwoWayTestSetUp ()
  {
    super ();
  }

  protected AbstractCEFTwoWayTestSetUp (@Nonnegative final int nRetries)
  {
    super (nRetries);
  }

  @BeforeClass
  public static void startServerNinety () throws Exception
  {
    WebAppListener.setOnlyOneInstanceAllowed (false);
    RunInJettyAS4TEST9090.startNinetyServer ();
  }

  @AfterClass
  public static void shutDownServerNinety () throws Exception
  {
    // reset
    RunInJettyAS4TEST9090.stopNinetyServer ();
    WebAppListener.setOnlyOneInstanceAllowed (true);
  }

  @Before
  public void setUpCEF ()
  {
    m_aESENSTwoWayPMode = ESENSPMode.createESENSPModeTwoWay (AS4TestConstants.CEF_INITIATOR_ID,
                                                             AS4TestConstants.CEF_RESPONDER_ID,
                                                             AS4TestConstants.DEFAULT_SERVER_ADDRESS,
                                                             IPModeIDProvider.DEFAULT_DYNAMIC);

    m_eSOAPVersion = m_aESENSTwoWayPMode.getLeg1 ().getProtocol ().getSOAPVersion ();
    try
    {
      m_aPayload = DOMReader.readXMLDOM (new ClassPathResource (AS4TestConstants.TEST_SOAP_BODY_PAYLOAD_XML));
    }
    catch (final SAXException ex)
    {
      throw new IllegalStateException ("Failed to parse example XML", ex);
    }
  }

  protected Document testSignedUserMessage (@Nonnull final ESOAPVersion eSOAPVersion,
                                            @Nullable final Node aPayload,
                                            @Nullable final ICommonsList <WSS4JAttachment> aAttachments,
                                            @Nonnull final AS4ResourceManager aResMgr) throws WSSecurityException
  {
    final Document aSignedDoc = SignedMessageCreator.createSignedMessage (AS4CryptoFactory.DEFAULT_INSTANCE,
                                                                          testUserMessageSoapNotSigned (aPayload,
                                                                                                        aAttachments),
                                                                          eSOAPVersion,
                                                                          aAttachments,
                                                                          aResMgr,
                                                                          true,
                                                                          ECryptoAlgorithmSign.SIGN_ALGORITHM_DEFAULT,
                                                                          ECryptoAlgorithmSignDigest.SIGN_DIGEST_ALGORITHM_DEFAULT);
    return aSignedDoc;
  }

  protected Document testUserMessageSoapNotSigned (@Nullable final Node aPayload,
                                                   @Nullable final ICommonsList <WSS4JAttachment> aAttachments)
  {
    // Add properties
    final ICommonsList <Ebms3Property> aEbms3Properties = MockEbmsHelper.getEBMSProperties ();

    final Ebms3MessageInfo aEbms3MessageInfo = MessageHelperMethods.createEbms3MessageInfo ();
    final Ebms3PayloadInfo aEbms3PayloadInfo = UserMessageCreator.createEbms3PayloadInfo (aPayload, aAttachments);

    final Ebms3CollaborationInfo aEbms3CollaborationInfo;
    final Ebms3PartyInfo aEbms3PartyInfo;
    aEbms3CollaborationInfo = UserMessageCreator.createEbms3CollaborationInfo (AS4TestConstants.TEST_ACTION,
                                                                               AS4TestConstants.TEST_SERVICE_TYPE,
                                                                               MockPModeGenerator.SOAP11_SERVICE,
                                                                               AS4TestConstants.TEST_CONVERSATION_ID,
                                                                               m_aESENSTwoWayPMode.getID (),
                                                                               MockEbmsHelper.DEFAULT_AGREEMENT);
    aEbms3PartyInfo = UserMessageCreator.createEbms3PartyInfo (CAS4.DEFAULT_SENDER_URL,
                                                               AS4TestConstants.CEF_INITIATOR_ID,
                                                               CAS4.DEFAULT_RESPONDER_URL,
                                                               AS4TestConstants.CEF_RESPONDER_ID);

    final Ebms3MessageProperties aEbms3MessageProperties = UserMessageCreator.createEbms3MessageProperties (aEbms3Properties);

    final AS4UserMessage aDoc = UserMessageCreator.createUserMessage (aEbms3MessageInfo,
                                                                      aEbms3PayloadInfo,
                                                                      aEbms3CollaborationInfo,
                                                                      aEbms3PartyInfo,
                                                                      aEbms3MessageProperties,
                                                                      m_eSOAPVersion)
                                                  .setMustUnderstand (true);
    return aDoc.getAsSOAPDocument (aPayload);
  }

}
