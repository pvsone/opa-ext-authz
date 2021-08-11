package policy["com.styra.envoy.ingress"].rules.rules

import input.attributes.request.http as http_request

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
}

action_allowed {
  http_request.method == "POST"
  token.payload.role == "admin"
}

token := {"valid": valid, "payload": payload} {
    [_, encoded] := split(http_request.headers.authorization, " ")
    [valid, _, payload] := io.jwt.decode_verify(encoded, {"secret": "secret"})
}
