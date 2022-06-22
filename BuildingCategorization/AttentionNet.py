import scipy.misc as misc
import torchvision.models as models
import torch
import copy
from torch.autograd import Variable
import numpy as np
import torch.nn as nn
import torch.nn.functional as F

class Net(nn.Module):

        def __init__(self,NumClasses):
            super(Net, self).__init__()
            self.UseGPU=False
            self.Net = models.resnet50(pretrained=True)
            self.Net.fc=nn.Linear(2048, int(NumClasses))


        def AddAttentionLayer(self):
            self.ValeLayers = nn.ModuleList()
            self.Valve = {}
            self.BiasValve = {}
            ValveDepths = [64]
            for i, dp in enumerate(ValveDepths):
                self.Valve[i] = nn.Conv2d(1, dp, stride=1, kernel_size=3, padding=1, bias=True)
                self.Valve[i].bias.data = torch.zeros(self.Valve[i].bias.data.shape)
                self.Valve[i].weight.data = torch.zeros(self.Valve[i].weight.data.shape)
            for i in self.Valve:
                self.ValeLayers.append(self.Valve[i])



        def forward(self,Images,ROI,EvalMode=False):
                InpImages = torch.autograd.Variable(torch.from_numpy(Images), requires_grad=False).transpose(2,3).transpose(1, 2).type(torch.FloatTensor)
                ROImap = torch.autograd.Variable(torch.from_numpy(ROI.astype(np.float)), requires_grad=False).unsqueeze(dim=1).type(torch.FloatTensor)

                RGBMean = [123.68, 116.779, 103.939]
                RGBStd = [65, 65, 65]
                for i in range(len(RGBMean)): InpImages[:, i, :, :]=(InpImages[:, i, :, :]-RGBMean[i])/RGBStd[i]

                nValve = 0
                x=InpImages
                x = self.Net.conv1(x)
                AttentionMap = self.Valve[nValve](F.interpolate(ROImap, size=x.shape[2:4], mode='bilinear'))
                x = x + AttentionMap
                nValve += 1

                x = self.Net.bn1(x)
                x = self.Net.relu(x)
                x = self.Net.maxpool(x)
                x = self.Net.layer1(x)
                x = self.Net.layer2(x)
                x = self.Net.layer3(x)
                x = self.Net.layer4(x)

                x = torch.mean(torch.mean(x, dim=2), dim=2)
                x = self.Net.fc(x)
                ProbVec = F.softmax(x,dim=1)
                Prob,Pred=ProbVec.max(dim=1)

                return ProbVec,Pred