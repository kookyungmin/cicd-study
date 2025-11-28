FROM jenkins/jenkins:lts

USER root

RUN apt-get update && \
    apt-get install -y docker.io && \
    rm -rf /var/lib/apt/lists/*

RUN groupadd -f docker && usermod -aG docker jenkins

# start.sh: root가 dockerd 켠 뒤 jenkins로 Jenkins 실행
RUN printf '#!/bin/bash\n' > /usr/local/bin/start.sh && \
    printf 'dockerd -H unix:///var/run/docker.sock &\n' >> /usr/local/bin/start.sh && \
    printf 'su jenkins -c "/usr/bin/tini -- /usr/local/bin/jenkins.sh"\n' >> /usr/local/bin/start.sh && \
    chmod +x /usr/local/bin/start.sh

# ENTRYPOINT는 root → root가 dockerd 켜고 → Jenkins는 jenkins 계정으로 실행됨
ENTRYPOINT ["/usr/local/bin/start.sh"]