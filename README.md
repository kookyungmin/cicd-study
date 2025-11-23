# CI / CD Study

Github Actions, Jenkins 를 활용한 CI / CD Study

<!-- prettier-ignore-start -->
![Nginx](https://shields.io/badge/nginx-black?logo=nginx&style=for-the-badge%22)
![Git](https://shields.io/badge/git-black?logo=git&style=for-the-badge%22)
![GithubActions](https://shields.io/badge/github_actions-black?logo=githubactions&style=for-the-badge%22)
![Jenkins](https://shields.io/badge/jenkins-black?logo=jenkins&style=for-the-badge%22)

<!-- prettier-ignore-end -->

### System Requirements

- [Nginx]
- [Git]
- [Github Actions]
- [Jekins]

### 유용한 git 명령어

```git restore --staged [file-name] : staged 상태에 있는 파일을 다시 되돌림```

```git checkout -b [브랜치명] : 해당 브랜치로 checkout 할 때, 브랜치가 없으면 생성해줌```

```
git rebase [base 로 지정할 브랜치 명] : 뿌리(base branch)를 바꿔서 병합 -> 같은 feature에 대해서는 rebase을 활용하면 history 관리가 편함
ex) main 브랜치에서 feat/#1, feat/#2 브랜치를 생성 후 작업하고 나서, 
feat/#2 를 feat/#1 브랜치로 rebase 후 feat/#1 브랜치를 main 에 merge (줄기가 한개 생성됨)
```

```
git rebase -i HEAD~4 : 현재 commit 된 시점부터 4번째 커밋까지 합침 (단, merge commit 은 제외)
가장 오래된 commit 은 남기고, 나머지를 s(squash)로 변경
```

```
git reset --soft HEAD~2 : 현재 commit 된 시점부터 2번째 커밋까지 되돌림 (merge commit 합칠때 유용)

```

```git commit --amend : 커밋 메시지 수정```
