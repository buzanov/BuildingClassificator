import tifffile
import numpy as np
import AttentionNet as Net
import matplotlib.pyplot as plt
import OpenSurfacesClasses
import cv2
import torch
import scipy.misc as misc
import numpy as np
import os

os.environ['KMP_DUPLICATE_LIB_OK']='True'
Trained_model_path=r"C:\Users\Vla1z\PycharmProjects\diplom\classifier\logs\9000.torch"
ImageFile=r"D:\BuildingClassificator\AOI_3_Paris_Train\RGB-PanSharpen\RGB-PanSharpen_AOI_3_Paris_img118.tif"
ROIMaskFile= r"D:\BuildingClassificator\screencast\masks\RGB-PanSharpen_AOI_3_Paris_img118_20.png"
NumClasses=27

Net=Net.Net(NumClasses=NumClasses,UseGPU=False)
Net.AddAttentionLayer()
Net.load_state_dict(torch.load(Trained_model_path))

Net.eval()
Images=cv2.imread(ImageFile)
ROIMask=cv2.imread(ROIMaskFile,0)

Images = Images[..., :: -1]
img = tifffile.imread(ImageFile)
tifffile.imshow(img)
plt.show()
imgplot=plt.imshow(ROIMask*255)
plt.show()

Images=np.expand_dims(Images,axis=0).astype(np.float32)
ROIMask=np.expand_dims(ROIMask,axis=0).astype(np.float32)

Prob, PredLb = Net.forward(Images, ROI=ROIMask,EvalMode=True)
PredLb = PredLb.data.cpu().numpy()
Prob = Prob.data.cpu().numpy()

dic=OpenSurfacesClasses.CreateMaterialDict()
print("Predicted Label " + dic[PredLb[0]])
print("Predicted Label Prob="+str(Prob[0,PredLb[0]]*100)+"%")



