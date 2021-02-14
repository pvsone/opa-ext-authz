package rules

# bob is alice's manager, and betty is charlie's.
subordinates = {"alice": [], "charlie": [], "bob": ["alice"], "betty": ["charlie"]}

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
  subordinates[input.user][_] == username
}
