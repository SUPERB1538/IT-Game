// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/wubingze/Desktop/workspace_java_backend/UofG/ITSD-DT2025-26-Template/conf/routes
// @DATE:Wed Jan 21 05:17:30 CST 2026


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
