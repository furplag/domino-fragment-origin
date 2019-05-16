package jp.furplag.sandbox.domino.misc.marker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * a marker annotation to handle the field which has defined as a domain object .
 *
 * @author furplag
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainsAware {

  /** the name of field . */
  String value();

  /** the type of field . */
  Class<?> type();
}
