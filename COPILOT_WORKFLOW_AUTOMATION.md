# Copilot Workflow Automation - Full Lifecycle Management

## Overview

The `copilot-generate-tests.yml` workflow now includes complete lifecycle automation:
1. Creates/updates GitHub Issue assigned to @copilot
2. Waits for Copilot Agent to respond (PR creation or issue close)
3. Automatically closes issue with detailed summary
4. Triggers CI workflow on the agent's branch
5. Posts workflow summary to GitHub Actions UI

## Workflow Architecture

### Step 1: Issue Creation
```yaml
- Creates issue with comprehensive test requirements
- Assigns to @copilot to trigger Copilot Agent
- Analyzes pom.xml for dependencies
- Detects existing test frameworks
- Outputs: issue_number
```

### Step 2: Wait for Agent Response
```yaml
- Polls every 30 seconds for up to 30 minutes
- Checks for:
  * PR created by copilot-swe-agent[bot]
  * Issue closed by agent (no tests needed)
  * Timeout after max attempts
- Outputs: pr_created, pr_number, pr_branch, agent_action
```

### Step 3: Close Issue with Summary
```yaml
- Always runs (even on failure)
- Generates context-aware summary:
  * PR created: Links to PR, next steps
  * No PR: Explains why agent skipped
  * Timeout: Troubleshooting guidance
- Closes issue if PR created or agent closed it
- Leaves issue open on timeout for manual review
```

### Step 4: Trigger CI
```yaml
- Only runs if PR was created
- Dispatches ci.yml workflow on agent's branch
- Posts comment to PR confirming CI trigger
- Ensures tests are validated before merge
```

### Step 5: Workflow Summary
```yaml
- Posts summary to GitHub Actions UI
- Shows: issue number, agent action, PR details, CI status
- Visible in Actions tab for quick status check
```

## Agent Actions and Outcomes

### ✅ Scenario 1: PR Created Successfully

**Timeline:**
1. Issue created and assigned to @copilot
2. Agent analyzes code on remote branch
3. Agent creates PR with tests
4. Workflow detects PR within 30 min
5. Issue closed with success summary
6. CI triggered on PR branch

**Issue Summary:**
```markdown
## 🤖 Copilot Agent Workflow Summary

✅ **Status**: PR Created Successfully

- **PR**: #42
- **Branch**: `copilot/generate-tests-for-order-service`
- **Next Steps**:
  1. CI pipeline will run on the PR branch
  2. Review the generated tests in PR #42
  3. Merge the PR if tests are satisfactory

Closing this issue as the tests have been generated.
```

**PR Comment:**
```markdown
🚀 CI workflow triggered automatically to verify generated tests.
```

### ℹ️ Scenario 2: No PR Needed

**Timeline:**
1. Issue created and assigned to @copilot
2. Agent analyzes code
3. Agent determines no tests needed (already exist, no changes, etc.)
4. Agent closes issue directly
5. Workflow detects closed issue
6. Issue updated with explanation summary

**Issue Summary:**
```markdown
## 🤖 Copilot Agent Workflow Summary

ℹ️ **Status**: No PR Created

The Copilot Agent closed this issue without creating a PR.
Possible reasons:
- No test generation was needed
- Tests already exist for the changed files
- Agent determined the changes don't require new tests
```

### ⏱️ Scenario 3: Timeout

**Timeline:**
1. Issue created and assigned to @copilot
2. Workflow waits 30 minutes
3. No PR created, issue still open
4. Workflow times out
5. Issue left open with troubleshooting summary

**Issue Summary:**
```markdown
## 🤖 Copilot Agent Workflow Summary

⏱️ **Status**: Timeout

The workflow timed out waiting for Copilot Agent to create a PR.
Possible reasons:
- Agent is still processing (check back later)
- Agent encountered an error
- Issue was not properly assigned to @copilot

**Action Required**: Check issue status and reassign to @copilot if needed.
```

## Implementation Across Repositories

All 4 repositories now have the enhanced workflow:

### ✅ beneficiaries
- File: `.github/workflows/copilot-generate-tests.yml`
- Commit: `d690a74` - "Add automated issue closing and CI triggering to Copilot workflow"
- Status: Committed and ready

### ✅ paymentConsumer
- File: `.github/workflows/copilot-generate-tests.yml`
- Commit: `ecedc20` - "Add Copilot test generation workflow with auto-close and CI trigger"
- Status: Committed (new workflow created)
- Also includes: Pre-commit hook warn mode update

### ✅ paymentprocessor
- File: `.github/workflows/copilot-generate-tests.yml`
- Commit: `59a498c` - "Add Copilot test generation workflow with auto-close and CI trigger"
- Status: Committed (new workflow created)
- Also includes: Pre-commit hook warn mode update

### ✅ sit-test-repo
- File: `.github/workflows/copilot-generate-tests.yml`
- Commit: `0d9cb6f` - "Add Copilot test generation workflow with auto-close and CI trigger"
- Status: Committed (new workflow created)
- Also includes: Pre-commit hook warn mode update, multi-module support

## Technical Details

### Polling Logic

```javascript
const maxAttempts = 60;      // 60 attempts
const delayMs = 30000;        // 30 seconds between attempts
// Total wait time: 30 minutes
```

**Why 30 minutes?**
- Copilot Agent needs time to:
  - Analyze codebase
  - Generate unit tests
  - Generate integration tests
  - Generate BDD scenarios
  - Run tests for validation
  - Create PR with all changes
- Complex services may take 10-20 minutes
- Timeout provides buffer for agent processing

### PR Detection Algorithm

1. **List open PRs** sorted by creation date (newest first)
2. **Filter by author**: `copilot-swe-agent[bot]` or type `Bot`
3. **Check issue timeline** for cross-reference events
4. **Verify PR body** contains issue number `#N`
5. **First match wins**: Return PR number and branch

### Issue Close Conditions

| Condition | Issue State | Reason |
|-----------|-------------|--------|
| PR created | Closed | Tests generated successfully |
| Agent closed issue | Closed | Agent determined no tests needed |
| Timeout | Open | Manual review required |
| Workflow failure | Open | Error needs investigation |

### CI Trigger Mechanism

```javascript
// Uses workflow_dispatch to trigger ci.yml
await github.rest.actions.createWorkflowDispatch({
  owner: context.repo.owner,
  repo: context.repo.repo,
  workflow_id: 'ci.yml',
  ref: prBranch  // Agent's branch, not main
});
```

**Requirements:**
- `ci.yml` must have `workflow_dispatch` trigger
- Workflow must support running on any branch
- Permissions must allow workflow dispatch

## Complete End-to-End Flow

### Developer Workflow

```bash
# 1. Developer commits code without tests
git add src/main/java/OrderService.java
git commit -m "Add order processing"

# Pre-commit hook warns (doesn't block):
⚠️  WARNING: Committing without tests. GitHub Issue created for Copilot Agent.
Copilot will generate tests automatically in a separate PR.

# 2. Push to remote
git push origin main

# 3. Wait for Copilot (automated from here)
# - GitHub Issue created: #42
# - Assigned to @copilot
# - Copilot reacts: 👀
# - Copilot creates PR: #43
# - Issue auto-closed with summary
# - CI triggered on PR branch

# 4. Review generated tests
gh pr view 43

# 5. Merge when ready
gh pr merge 43 --squash
```

### GitHub Actions Workflow

```
Trigger: workflow_dispatch
  ↓
Checkout target branch
  ↓
Analyze dependencies (pom.xml)
  ↓
Create/update issue → Issue #42
  ↓
Assign to @copilot
  ↓
Wait for agent response (poll 30 min)
  ↓
┌─────────────────┬───────────────────┬──────────────┐
│  PR Created     │  Issue Closed     │   Timeout    │
│  (agent action) │  (no PR needed)   │  (still open)│
└─────────────────┴───────────────────┴──────────────┘
  ↓                 ↓                   ↓
Close issue       Already closed      Leave open
with summary      with summary        with guidance
  ↓
Trigger CI on PR branch
  ↓
Post workflow summary
```

## Workflow Outputs

### GitHub Actions Summary
Visible in the Actions tab:

```markdown
## Workflow Completed

- **Issue**: #42
- **Agent Action**: created_pr
- **PR Created**: #43
- **Branch**: copilot/generate-tests-for-order-service
- **CI Triggered**: ✅
```

### Issue Comment (Success)
```markdown
## 🤖 Copilot Agent Workflow Summary

✅ **Status**: PR Created Successfully

- **PR**: #43
- **Branch**: `copilot/generate-tests-for-order-service`
- **Next Steps**:
  1. CI pipeline will run on the PR branch
  2. Review the generated tests in PR #43
  3. Merge the PR if tests are satisfactory

Closing this issue as the tests have been generated.
```

### PR Comment
```markdown
🚀 CI workflow triggered automatically to verify generated tests.
```

## Benefits

### 🎯 Zero Manual Intervention
- Issue creation → automated
- Agent triggering → automated
- Issue closing → automated
- CI triggering → automated
- Summary generation → automated

### 📊 Complete Observability
- Workflow summary in Actions UI
- Issue summary explains outcome
- PR comment confirms CI trigger
- All steps logged with timestamps

### 🔄 Handles All Scenarios
- ✅ Success: PR created, issue closed, CI running
- ℹ️ No action needed: Issue closed with explanation
- ⏱️ Timeout: Issue left open for manual review
- ❌ Failure: Issue left open for debugging

### 🚀 Asynchronous Workflow
- Developer pushes code immediately
- Copilot works in background
- CI validates generated tests
- Developer reviews when ready

### 🧪 CI Validation Built-In
- Tests run automatically on PR branch
- Ensures 80% coverage before merge
- Catches issues early
- No manual CI trigger needed

## Troubleshooting

### Issue Created But No PR After 30 Minutes

**Check:**
1. Is issue assigned to @copilot? `gh issue view <number>`
2. Did Copilot react with 👀? (Check issue timeline)
3. Check Copilot quota/subscription status
4. Look for open PRs from copilot-swe-agent[bot]

**Solution:**
- Reassign to @copilot if missing
- Wait longer if agent reacted but no PR yet
- Check Copilot status page

### CI Not Triggering on PR Branch

**Check:**
1. Does `ci.yml` have `workflow_dispatch` trigger?
2. Are workflow permissions correct?
3. Check Actions tab for dispatch events

**Solution:**
```yaml
# Add to ci.yml if missing:
on:
  workflow_dispatch:  # Enable manual/API trigger
  pull_request:
  push:
    branches: [ main ]
```

### Issue Closed But No Summary

**Check:**
1. Workflow logs in Actions tab
2. Script step output for errors
3. GitHub API rate limits

**Solution:**
- Re-run workflow if it failed
- Check for GitHub API issues
- Ensure `issues: write` permission

### Multiple PRs From Copilot

**Behavior:**
- Workflow picks the FIRST matching PR
- Uses creation date (newest first)
- Checks for issue cross-reference

**Solution:**
- Normal behavior if multiple issues active
- Each issue links to correct PR
- Close duplicate PRs if needed

## Testing the Workflow

### Manual Trigger

```bash
# Trigger workflow manually
gh workflow run copilot-generate-tests.yml \
  --field branch=main \
  --field changed_files="src/main/java/OrderService.java"

# Watch workflow progress
gh run watch
```

### End-to-End Test

```bash
# 1. Create new service without tests
cat > src/main/java/TestService.java << 'EOF'
package com.example;
public class TestService {
    public String process(String input) {
        return input.toUpperCase();
    }
}
EOF

# 2. Commit and push
git add src/main/java/TestService.java
git commit -m "Add test service"
git push origin main

# 3. Monitor issue creation
gh issue list --label copilot-agent

# 4. Wait for PR (or check workflow)
gh run list --workflow=copilot-generate-tests.yml

# 5. Review generated PR
gh pr list --author "app/copilot-swe-agent"
```

## Configuration

### Adjust Wait Time

Edit `.github/workflows/copilot-generate-tests.yml`:

```javascript
// Increase to 60 minutes:
const maxAttempts = 120;  // 120 * 30s = 60 min
const delayMs = 30000;

// Decrease to 15 minutes:
const maxAttempts = 30;   // 30 * 30s = 15 min
const delayMs = 30000;
```

### Change Polling Interval

```javascript
// Poll every minute instead of 30 seconds:
const delayMs = 60000;  // 60 seconds
const maxAttempts = 30; // Still 30 minutes total
```

### Customize Summary Messages

Edit the `Close issue with summary` step:

```javascript
summaryComment += `✅ **Status**: PR Created Successfully\n\n`;
summaryComment += `Custom message here...\n`;
```

## Next Steps

1. **Push changes to remote**:
   ```bash
   cd beneficiaries && git push origin main
   cd ../paymentConsumer && git push origin main
   cd ../paymentprocessor && git push origin main
   cd ../sit-test-repo && git push origin main
   ```

2. **Test with real commit**:
   - Create Java file without tests
   - Commit and push
   - Verify issue creation
   - Wait for Copilot PR
   - Confirm auto-close and CI trigger

3. **Monitor workflow**:
   - Check Actions tab for workflow runs
   - Review issue summaries
   - Verify CI runs on PR branches

4. **Iterate based on feedback**:
   - Adjust wait times if needed
   - Customize summary messages
   - Add additional automation steps

## Summary

The Copilot workflow now provides **complete lifecycle automation**:

- ✅ **Issue Management**: Auto-create, auto-close with context-aware summaries
- ✅ **Agent Integration**: Seamless Copilot Agent triggering and monitoring
- ✅ **CI/CD Integration**: Automatic CI trigger on generated test PRs
- ✅ **Observability**: Full visibility via summaries, comments, and logs
- ✅ **Error Handling**: Graceful handling of timeout, no-PR, and failure scenarios

Developers can now commit code without tests, and the entire test generation, validation, and review process happens automatically in the background.
