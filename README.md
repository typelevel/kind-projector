# Kind Projector

## Dedication

> "But I don't want to go among mad people," Alice remarked.
> 
> "Oh, you can't help that," said the Cat: "we're all mad here. I'm mad.
> You're mad."
> 
> "How do you know I'm mad?" said Alice.
> 
> "You must be," said the Cat, "or you wouldn't have come here."
>  
> --Lewis Carroll, "Alice's Adventures in Wonderland"

## Overview

One piece of Scala syntactic noise that often trips people up is the use of
type projections to implement anonymous, partiall-applied types. For example:

```scala
// partially-applied type named "IntOrA"
type IntOrA[A] = Either[Int, A]

// type projection implementing the same type anonymously (without a name).
({type L[A] = Either[Int, A]})#L
```

Many people have wished for a better way to do this.

The goal of this plugin is to add a syntax for type lambdas. We do this by
rewriting syntactically valid programs into new programs, letting us seem to
add new keywords to the language. This is done via an (un-typed) tree
transformation via a compiler plugin.

One problem with this approach is that it changes the meaning of (potentially)
valid programs. In practice this means that you must avoid defining the
following identifiers:

 1. Lambda
 2. ?
 3. L_kp
 4. X_kp0, X_kp1, ...

If you find yourself using lots of type lambdas, and you don't mind reserving
those identifiers, then this compiler plugin is for you!

## Examples

Two syntaxes are supported. The first resembles the `_ + _` syntax for
anonymous functions and turns things like: 

```scala
Either[?, Double]
Tuple3[Int, ?, ?]
```

into type projections like:

```scala
({type L_kp[X_kp0] = Either[X_kp0, Double]})#L_kp
({type L_kp[X_kp1, X_kp2] = Tuple3[Int, X_kp1, X_kp2]})#L_kp
```

The second resembles the `(x, y) => x + y` syntax for anonymous functions and
turns things like:

```scala
Lambda[A => (A, A)]
Lambda[(A, B) => Either[A, Option[B]]]
```

into type projections like:

```scala
({type L_kp[A] = (A, A)})#L_kp
({type L_kp[A, B] = Either[A, Option[B]]})#L_kp
```

## Building

For various reasons I've been using make to develop the plugin. Here are
some various make targets:

    make plugin        compile and assemble the plugin jar
    make test          (try to) compile the test code
    make tree          print the tree of the test code
    make clean         delete jar, class files, etc

There are more targets--the Makefile should be fairly readable.

## Disclaimers and Errata

There have been suggestions for better syntax, like `[A,B]Either[B,A]`
instead of `Lambda[(A, B) => Either[B, A]]`. Unfortunately this would actually
require modifying the parser (i.e. the language itself) which is outside the
scope of this project (at least, until there is an earlier compiler phase to
plug into).

This is only working in the most fragile sense. If you try "fancy" things
like `Either[Int, ?][Double]`, you will probably not like the result. Also,
please do not use this code in production unless you know what you're doing;
this project is clearly an abuse of the compiler plugin framework and the
author disclaims all warranty or liability.

That said, if you end up using this, even in a toy project, please let me know!

## Copyright and License

All code is available to you under the MIT license, available at
http://opensource.org/licenses/mit-license.php. 

Copyright Erik Osheim, 2011-2012.
