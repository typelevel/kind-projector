// package pc
// 
// import org.junit._
// import org.ensime.pcplod._
// 
// class PresentationCompilerTest {
//   import Assert._
// 
//   @Test
//   def notReportErrorsInValidCode(): Unit = withMrPlod("moo.scala") { mr: MrPlod =>
//     assertTrue(mr.messages.toString, mr.messages.size == 0)
// 
//     assertEquals(Some("[F[_]]()Unit"), mr.typeAtPoint('test))
// 
//     // failing to return type information here
//     //assertNotEquals(Some("<notype>"), mr.typeAtPoint('either))
//     //assertNotEquals(Some("<notype>"), mr.typeAtPoint('throwable))
//   }
// 
// }
