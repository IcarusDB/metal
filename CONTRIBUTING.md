# Contributing to Metal

Metal is Open Source software, licensed under the [Apache 2.0. license](LICENSE).
The main benefit of Open Source is that you don't need to wait for a vendor to provide a fix or a feature.
If you've got the skills (and the will), it's already at your fingertips.

There are multiple ways to contribute:

1. [Reporting an issue](#issue-reports)
2. [Sending a pull request](#pull-requests).
Note that you don't need to be a developer to help us.
Contributions that improve the documentation are always appreciated.

If you feel yourself in need of assistance, please reach us directly via [Discussions/Assistance](https://github.com/CheneyYin/metal/discussions/28).

## Issue Reports

Thanks for reporting your issue.
To help us resolve your issue quickly and efficiently, we need as much data for diagnostics as possible.
Please share with us the following information:

1.	Exact Metal version that you use (_e.g._ `1.0.0`, also whether it is a minor release, or the latest snapshot).
2.	Operating system.
If it is Linux, kernel version is helpful.
3.	Java version.
It is also helpful to mention the JVM parameters.
4.	Logs and stack traces, if available.
5.	Detailed description of the steps to reproduce your issue.

## Pull requests

Thanks a lot for creating your <abbr title="Pull Request">PR</abbr>!

A PR can target many different subjects:

* Fix a bug
* Add a feature
* Add additional tests to improve the test coverage, or fix flaky tests
* Anything else that makes Metal better!

All PRs follow the same process:

1.	Contributions are submitted, reviewed, and accepted using the PR system on GitHub.
2.	For first time contributors, our bot will automatically ask you to sign the Metal Contributor Agreement on the PR.
3.	The latest changes are in the `master` branch.
4.	Make sure to design clean commits that are easily readable.
That includes descriptive commit messages.
5.	Please keep your PRs as small as possible, _i.e._ if you plan to perform a huge change, do not submit a single and large PR for it.
For an enhancement or larger feature, you can create a GitHub issue first to discuss.
6.	Before you push, run the command `mvn clean splotess:check` in your terminal and fix the `spotless` errors if any.
Push your PR once it is free of 'spotless' errors.
7.	If you submit a PR as the solution to a specific issue, please mention the issue number either in the PR description or commit message.
