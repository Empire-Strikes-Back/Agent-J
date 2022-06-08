#!/bin/bash


install(){
  npm i --no-package-lock
}

copy(){
  mkdir -p out/jar/ui/
  mkdir -p out/jar/src/Agent-J/
  cp src/Agent_J/index.html out/jar/ui/index.html
  cp src/Agent_J/style.css out/jar/ui/style.css
  cp package.json out/jar/package.json
  cp out/identicon/icon.png out/jar/icon.png
}

shadow(){
  clj -A:shadow:main:ui -M -m shadow.cljs.devtools.cli "$@"
}

repl(){
  install
  copy
  shadow clj-repl
  # (shadow/watch :main)
  # (shadow/watch :ui)
  # (shadow/repl :main)
  # :repl/quit
}

identicon(){
  clojure \
    -X:Zazu Zazu.core/process \
    :word '"Agent-J"' \
    :filename '"out/identicon/icon.png"' \
    :size 256
}

compile(){
  shadow release :ui :main
}

release(){
  rm -rf out
  install
  identicon
  copy
  compile
  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  mv out/jar/main.js "out/jar/Agent-J-$COMMIT_COUNT-$COMMIT_HASH.js"
}

run(){
  npx electron out/jar/main.js &>/dev/null &
}

tag(){
  COMMIT_HASH=$(git rev-parse --short HEAD)
  COMMIT_COUNT=$(git rev-list --count HEAD)
  TAG="$COMMIT_COUNT-$COMMIT_HASH"
  git tag $TAG $COMMIT_HASH
  echo $COMMIT_HASH
  echo $TAG
}

"$@"