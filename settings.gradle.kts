rootProject.name = "home-automation"
include("backend:server")
include("backend:node")
project(":backend:server").name = "home-automation-server"
project(":backend:node").name = "home-automation-node"
//include("backend")
include("ui")
