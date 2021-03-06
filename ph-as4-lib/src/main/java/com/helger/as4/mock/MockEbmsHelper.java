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
package com.helger.as4.mock;

import javax.annotation.Nonnull;

import com.helger.as4.messaging.domain.MessageHelperMethods;
import com.helger.as4.model.pmode.leg.PModeLegProtocol;
import com.helger.as4lib.ebms3header.Ebms3Property;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.ICommonsList;

/**
 * This class helps to keep the test clean from strings that can be changed.
 * Most of the test use the predefined settings here.
 *
 * @author bayerlma
 * @author Philip Helger
 */
public final class MockEbmsHelper
{
  public static final String DEFAULT_AGREEMENT = "urn:as4:agreements:so-that-we-have-a-non-empty-value";
  public static final String SOAP_12_PARTY_ID = "APP_000000000012";
  public static final String SOAP_11_PARTY_ID = "APP_000000000011";

  private MockEbmsHelper ()
  {}

  @Nonnull
  public static PModeLegProtocol createMockProtocol ()
  {
    return PModeLegProtocol.createForDefaultSOAPVersion ("http://test.example.org");
  }

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsList <Ebms3Property> getEBMSProperties ()
  {
    return MessageHelperMethods.createEmbs3PropertiesSpecial ("C1-test", "C4-test");
  }
}
