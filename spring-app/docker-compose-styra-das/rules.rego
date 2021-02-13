package rules

import data.dataset

main := {
  "allowed": allow
}

default allow = false

# Allow users to get their own salaries.
allow {
  some username
  input.method == "GET"
  input.path = ["finance", "salary", username]
  input.user == username
}

# Allow managers to get their subordinates' salaries.
allow {
  some username
  input.method == "GET"
  input.path = ["finance", "salary", username]
  dataset.subordinates[input.user][_] == username
}
