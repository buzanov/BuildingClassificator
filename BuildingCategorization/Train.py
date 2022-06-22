import numpy as np
import AttentionNet as Net
import Reader as Reader
import os
import scipy.misc as misc
import torch

ImageDir=r"D:\BuildingClassificator\AOI_3_Paris_Train\RGB-PanSharpen"
AnnotationDir=r"D:\BuildingClassificator\BuildingClassificator\fullmasks"
UseCuda=False
MinSize=120
MaxSize=650
MaxBatchSize=20
MaxPixels=800*800*3.
logs_dir= "classifier/logs/"
if not os.path.exists(logs_dir): os.makedirs(logs_dir)


Trained_model_path=""
Learning_Rate=1e-5
learning_rate_decay=0.999999#


TrainLossTxtFile=logs_dir+"TrainLoss.txt"
ValidLossTxtFile=logs_dir+"ValidationLoss.txt"
Weight_Decay=1e-5
MAX_ITERATION = int(10000)
NumClasses=-1
BackgroundClass=0

Reader=Reader.Reader(ImageDir=ImageDir,AnnotationDir=AnnotationDir, MaxBatchSize=MaxBatchSize,MinSize=MinSize,MaxSize=MaxSize,MaxPixels=MaxPixels, AnnotationFileType="png", ImageFileType="tif",BackgroundClass=0, NumClasses=NumClasses)

if NumClasses ==-1: NumClasses = Reader.NumClass+1

Net=Net.Net(NumClasses=NumClasses,UseGPU=False)
Net.AddAttentionLayer()

optimizer=torch.optim.Adam(params=Net.parameters(),lr=Learning_Rate,weight_decay=Weight_Decay)

f = open(TrainLossTxtFile, "a")
f.write("Iteration\tloss\t Learning Rate="+str(Learning_Rate))
f.close()
AVGLoss=0

for itr in range(1,MAX_ITERATION):
    Images, SegmentMask, Labels, LabelsOneHot = Reader.ReadNextBatchRandom(EqualClassProbabilty=False)

    Prob, Lb=Net.forward(Images,ROI=SegmentMask)
    Net.zero_grad()
    OneHotLabels=torch.autograd.Variable(torch.from_numpy(LabelsOneHot), requires_grad=False)
    Loss = -torch.mean((OneHotLabels * torch.log(Prob + 0.0000001)))
    if AVGLoss==0:  AVGLoss=float(Loss.data.cpu().numpy())
    else: AVGLoss=AVGLoss*0.999+0.001*float(Loss.data.cpu().numpy())
    Loss.backward()
    optimizer.step()

    if itr % 3000 == 0 and itr>0:
        print("Saving Model to file in "+logs_dir)
        torch.save(Net.state_dict(), logs_dir+ "/" + str(itr) + ".torch")
        print("model saved")

    if itr % 10==0:
        print("Step "+str(itr)+" Train Loss="+str(float(Loss.data.cpu().numpy()))+" Runnig Average Loss="+str(AVGLoss))
        with open(TrainLossTxtFile, "a") as f:
            f.write("\n"+str(itr)+"\t"+str(float(Loss.data.cpu().numpy()))+"\t"+str(AVGLoss))
            f.close()

