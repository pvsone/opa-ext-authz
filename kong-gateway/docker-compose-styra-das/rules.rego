package rules

import input as http_request

default allow = false

allow {
    is_token_valid
    action_allowed
}

is_token_valid {
  token.valid
  now := time.now_ns() / 1000000000
  token.payload.nbf <= now
  now < token.payload.exp
}

action_allowed {
  http_request.method == "GET"
  token.payload.role == "guest"
  glob.match("/people", ["/"], http_request.path)
}

action_allowed {
  http_request.method == "GET"
  token.payload.role == "admin"
  glob.match("/people", ["/"], http_request.path)
}

action_allowed {
  http_request.method == "POST"
  token.payload.role == "admin"
  glob.match("/people", ["/"], http_request.path)
}

token = http_request.token
