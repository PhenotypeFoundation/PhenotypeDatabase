package org.dbxp.moduleBase

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Use this annotation to tell the basefilters to refresh
 * user information, even though no authentication is required
 * @author robert
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD, ElementType.TYPE])
public @interface RefreshUserInformation {

}