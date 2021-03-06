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
package com.helger.as4.servlet;

import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.http.EHttpMethod;
import com.helger.commons.lang.StackTraceHelper;
import com.helger.commons.mime.CMimeType;
import com.helger.http.EHttpVersion;
import com.helger.servlet.response.UnifiedResponse;

/**
 * Special {@link UnifiedResponse} class with some sanity methods.
 *
 * @author Philip Helger
 */
public class AS4Response extends UnifiedResponse
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (AS4Response.class);

  public AS4Response (@Nonnull final EHttpVersion eHTTPVersion,
                      @Nonnull final EHttpMethod eHTTPMethod,
                      @Nonnull final HttpServletRequest aHttpRequest)
  {
    super (eHTTPVersion, eHTTPMethod, aHttpRequest);
    // Never cache the responses on client side
    disableCaching ();
    setAllowContentOnStatusCode (true);
  }

  public void setResponseError (@Nonnegative final int nStatusCode,
                                @Nonnull final String sMsg,
                                @Nullable final Throwable t)
  {
    s_aLogger.error ("HTTP " + nStatusCode + ": " + sMsg, t);

    String sBody = sMsg;
    if (t != null)
      sBody += "\nTechnical details:\n" + StackTraceHelper.getStackAsString (t);

    setContentAndCharset (sBody, StandardCharsets.UTF_8);
    setMimeType (CMimeType.TEXT_PLAIN);
    setStatus (nStatusCode);
  }
}
