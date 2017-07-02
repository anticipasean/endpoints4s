package endpoints
package documented
package algebra

import scala.language.higherKinds

/**
  * Algebra interface for describing URL including documentation.
  *
  * This interface is modeled after [[endpoints.algebra.Urls]] but some methods
  * take additional parameters carrying the documentation part.
  */
trait Urls {

  /** A query string carrying an `A` information */
  type QueryString[A]

  /** Provides convenient methods on [[QueryString]]. */
  implicit class QueryStringOps[A](first: QueryString[A]) {
    /**
      * Convenient method to concatenate two [[QueryString]]s.
      *
      * {{{
      *   qs[Int]("foo") & qs[String]("baz")
      * }}}
      *
      * @param second `QueryString` to concatenate with this one
      * @tparam B Information carried by the second `QueryString`
      * @return A `QueryString` that carries both `A` and `B` information
      */
    final def & [B](second: QueryString[B])(implicit tupler: Tupler[A, B]): QueryString[tupler.Out] =
      combineQueryStrings(first, second)
  }

  /** Concatenates two `QueryString`s */
  def combineQueryStrings[A, B](first: QueryString[A], second: QueryString[B])(implicit tupler: Tupler[A, B]): QueryString[tupler.Out]

  /**
    * Builds a `QueryString` with one parameter.
    *
    * @param name Parameter’s name
    * @tparam A Type of the value carried by the parameter
    */
  def qs[A](name: String)(implicit value: QueryStringParam[A]): QueryString[A]

  /**
    * Builds a `QueryString` with one optional parameter of type `A`.
    *
    * @param name Parameter’s name
    */
  def optQs[A](name: String)(implicit value: QueryStringParam[A]): QueryString[Option[A]]

  /**
    * A single query string parameter carrying an `A` information.
    */
  type QueryStringParam[A]

  /** Ability to define `String` query string parameters */
  implicit def stringQueryString: QueryStringParam[String]

  /** Ability to define `Int` query string parameters */
  implicit def intQueryString: QueryStringParam[Int]

  /** Query string parameter containing a `Long` value */
  implicit def longQueryString: QueryStringParam[Long]

  /**
    * An URL path segment carrying an `A` information.
    */
  type Segment[A]

  /** Ability to define `String` path segments */
  implicit def stringSegment: Segment[String]

  /** Ability to define `Int` path segments */
  implicit def intSegment: Segment[Int]

  /** Segment containing a `Long` value */
  implicit def longSegment: Segment[Long]

  /** An URL path carrying an `A` information */
  type Path[A] <: Url[A]

  /** Convenient methods for [[Path]]s. */
  implicit class PathOps[A](first: Path[A]) {
    /** Chains this path with the `second` constant path segment */
    final def / (second: String): Path[A] = chainPaths(first, staticPathSegment(second))
    /** Chains this path with the `second` path segment */
    final def / [B](second: Path[B])(implicit tupler: Tupler[A, B]): Path[tupler.Out] = chainPaths(first, second)
    /** Chains this path with the given [[QueryString]] */
    final def /? [B](qs: QueryString[B])(implicit tupler: Tupler[A, B]): Url[tupler.Out] = urlWithQueryString(first, qs)
  }

  /** Builds a static path segment */
  def staticPathSegment(segment: String): Path[Unit]

  /** Builds a path segment carrying an `A` information
    *
    * @param name Name for the segment (for documentation)
    */
  def segment[A](name: String)(implicit s: Segment[A]): Path[A]

  /** Chains the two paths */
  def chainPaths[A, B](first: Path[A], second: Path[B])(implicit tupler: Tupler[A, B]): Path[tupler.Out]

  /**
    * An empty path.
    *
    * Useful to begin a path definition:
    *
    * {{{
    *   path / "foo" / segment[Int]("some-value")
    * }}}
    *
    */
  val path: Path[Unit] = staticPathSegment("")

  /**
    * An URL carrying an `A` information
    */
  type Url[A]

  /** Builds an URL from the given path and query string */
  def urlWithQueryString[A, B](path: Path[A], qs: QueryString[B])(implicit tupler: Tupler[A, B]): Url[tupler.Out]

}
