"Version 2 deployed through CI/CD!"

aws eks update-kubeconfig \
  --region us-east-1 \
  --name devops-cluster

This creates/updates:          ~/.kube/config
If you want to see the file    cat ~/.kube/config
COPY AND SAVE THE WHOLE FILE, FIILENAME: kubeconfig.txt
to get the kubeconfig file for the CICD, upload "kubeconfig.txt at Jenkins credentials.... SECRET FILE 
							ID: kubeconfig
							Description: Eks kubeconfig
