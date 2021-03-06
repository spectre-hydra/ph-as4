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
package com.helger.as4.messaging.domain;

import java.util.Enumeration;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.http.HttpMessage;

import com.helger.as4.CAS4;
import com.helger.as4lib.ebms3header.Ebms3Description;
import com.helger.as4lib.ebms3header.Ebms3MessageInfo;
import com.helger.as4lib.ebms3header.Ebms3PartyId;
import com.helger.as4lib.ebms3header.Ebms3Property;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.http.HttpHeaderMap;
import com.helger.commons.random.RandomHelper;
import com.helger.commons.string.StringHelper;
import com.helger.datetime.util.PDTXMLConverter;

/**
 * This class contains every method, static variables which are used by more
 * than one message creating classes in this package.
 *
 * @author bayerlma
 * @author Philip Helger
 */
@Immutable
public final class MessageHelperMethods
{
  private MessageHelperMethods ()
  {}

  @Nonnull
  @Nonempty
  public static String createRandomConversationID ()
  {
    return CAS4.LIB_NAME + "@Conv" + RandomHelper.getRandom ().nextLong ();
  }

  @Nonnull
  @Nonempty
  public static String createRandomMessageID ()
  {
    return CAS4.LIB_NAME + "@" + UUID.randomUUID ().toString ();
  }

  /**
   * Create a new message info with a UUID as message ID.
   *
   * @return Never <code>null</code>.
   */
  @Nonnull
  public static Ebms3MessageInfo createEbms3MessageInfo ()
  {
    return createEbms3MessageInfo (createRandomMessageID (), null);
  }

  /**
   * Create a new message info with a UUID as message ID and a reference to the
   * previous message.
   *
   * @param sRefToMessageID
   *        The message ID of the referenced message. May be <code>null</code>.
   * @return Never <code>null</code>.
   */
  @Nonnull
  public static Ebms3MessageInfo createEbms3MessageInfo (@Nullable final String sRefToMessageID)
  {
    return createEbms3MessageInfo (createRandomMessageID (), sRefToMessageID);
  }

  /**
   * Create a new message info.
   *
   * @param sMessageID
   *        The message ID. May neither be <code>null</code> nor empty.
   * @param sRefToMessageID
   *        to set the reference to the previous message needed for two way
   *        exchanges
   * @return Never <code>null</code>.
   */
  @Nonnull
  public static Ebms3MessageInfo createEbms3MessageInfo (@Nonnull @Nonempty final String sMessageID,
                                                         @Nullable final String sRefToMessageID)
  {
    ValueEnforcer.notEmpty (sMessageID, "MessageID");

    final Ebms3MessageInfo aMessageInfo = new Ebms3MessageInfo ();

    aMessageInfo.setMessageId (sMessageID);
    if (StringHelper.hasText (sRefToMessageID))
      aMessageInfo.setRefToMessageId (sRefToMessageID);

    aMessageInfo.setTimestamp (PDTXMLConverter.getXMLCalendarNowUTC ());
    return aMessageInfo;
  }

  @Nonnull
  @ReturnsMutableCopy
  public static Ebms3Description createEbms3Description (@Nonnull final Locale aLocale, @Nonnull final String sText)
  {
    ValueEnforcer.notNull (aLocale, "Locale");
    ValueEnforcer.notNull (sText, "Text");

    final Ebms3Description aDesc = new Ebms3Description ();
    aDesc.setLang (aLocale.getLanguage ());
    aDesc.setValue (sText);
    return aDesc;
  }

  @Nonnull
  public static Ebms3Property createEbms3Property (@Nonnull @Nonempty final String sName, @Nonnull final String sValue)
  {
    final Ebms3Property aProp = new Ebms3Property ();
    aProp.setName (sName);
    aProp.setValue (sValue);
    return aProp;
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsList <Ebms3Property> createEmbs3PropertiesSpecial (@Nonnull final String sOriginalSender,
                                                                           @Nonnull final String sFinalRecipient)
  {
    return new CommonsArrayList <> (createEbms3Property (CAS4.ORIGINAL_SENDER, sOriginalSender),
                                    createEbms3Property (CAS4.FINAL_RECIPIENT, sFinalRecipient));
  }

  @Nonnull
  public static Ebms3PartyId createEbms3PartyId (@Nonnull final String sValue)
  {
    return createEbms3PartyId (null, sValue);
  }

  @Nonnull
  public static Ebms3PartyId createEbms3PartyId (@Nullable final String sType, @Nonnull final String sValue)
  {
    final Ebms3PartyId ret = new Ebms3PartyId ();
    ret.setType (sType);
    ret.setValue (sValue);
    return ret;
  }

  public static void moveMIMEHeadersToHTTPHeader (@Nonnull final MimeMessage aMimeMsg,
                                                  @Nonnull final HttpMessage aHttpMsg) throws MessagingException
  {
    ValueEnforcer.notNull (aMimeMsg, "MimeMsg");
    ValueEnforcer.notNull (aHttpMsg, "HttpMsg");

    // Move all mime headers to the HTTP request
    final Enumeration <Header> aEnum = aMimeMsg.getAllHeaders ();
    while (aEnum.hasMoreElements ())
    {
      final Header h = aEnum.nextElement ();
      // Make a single-line HTTP header value!
      aHttpMsg.addHeader (h.getName (), HttpHeaderMap.getUnifiedValue (h.getValue ()));

      // Remove from MIME message!
      aMimeMsg.removeHeader (h.getName ());
    }
  }
}
