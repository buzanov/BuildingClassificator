import numpy as np
import os
import random
import cv2


class Reader:
    def __init__(self, ImageDir,AnnotationDir, MaxBatchSize=100,MinSize=160,MaxSize=1100,MaxPixels=800*800*5, AnnotationFileType="png", ImageFileType="jpg",BackgroundClass=0, NumClasses=-1):
        self.ImageDir=ImageDir
        self.AnnotationDir=AnnotationDir
        self.MaxBatchSize=MaxBatchSize
        self.MinSize=MinSize
        self.MaxSize=MaxSize
        self.MaxPixels=MaxPixels
        self.AnnotationFileType=AnnotationFileType
        self.ImageFileType=ImageFileType
        self.BackgroundClass=BackgroundClass
        self.NumClass=NumClasses

        self.FileList=[]
        for FileName in os.listdir(AnnotationDir):
            if AnnotationFileType in FileName:
                self.FileList.append(FileName)

        print("Generating Class Map")
        self.ClassFiles={}
        self.ClassNumImages=np.zeros(500,dtype=np.int64)


        for n,FileName in enumerate(self.FileList):
            if n%100==0: print(str(n/len(self.FileList)*100)+"%")
            Lb=cv2.imread(AnnotationDir+"/"+FileName,0)
            MaxClass=Lb.max()
            self.NumClass = np.max([self.NumClass, MaxClass])
            for Class in range(self.NumClass+1):
                      if (Class!=BackgroundClass) and  (Class in Lb):
                          if not Class in self.ClassFiles:
                              self.ClassFiles[Class]=[]
                          self.ClassFiles[Class].append(FileName)
                          self.ClassNumImages[Class]+=1


        self.ClassNumImages = self.ClassNumImages[:self.NumClass + 1]
        if self.NumClass==-1:
            self.NumClass = int(self.NumClass)
        self.ImageN=0


    def ReadNextBatchRandom(self,EqualClassProbabilty=True,MinClassExample=1):

        Hb=np.random.randint(low=self.MinSize,high=self.MaxSize)
        Wb=np.random.randint(low=self.MinSize,high=self.MaxSize)
        BatchSize=np.int(np.min((np.floor(self.MaxPixels/(Hb*Wb)),self.MaxBatchSize)))
        BImgs=np.zeros((BatchSize,Hb,Wb,3))
        BSegmentMask=np.zeros((BatchSize,Hb,Wb))
        BLabels=np.zeros((BatchSize),dtype=np.int)
        BLabelsOneHot=np.zeros((BatchSize,self.NumClass+1),dtype=np.float32)

        for i in range(BatchSize):

            if EqualClassProbabilty:
                    ClassNum = np.random.randint(self.NumClass)
                    while self.ClassNumImages[ClassNum]<MinClassExample: ClassNum = np.random.randint(self.NumClass)
            else:
                    ClassNum=-1
                    Nm = np.random.randint(self.ClassNumImages.sum())+1
                    Sm=0
                    for cl in range(self.ClassNumImages.shape[0]):
                        Sm+=self.ClassNumImages[cl]
                        if (Sm>=Nm):
                            ClassNum=cl
                            break

            ImgNum = np.random.randint(self.ClassNumImages[ClassNum])
            Ann_name = self.ClassFiles[ClassNum][ImgNum]
            Img_name = self.ClassFiles[ClassNum][ImgNum].replace(self.AnnotationFileType,self.ImageFileType)

            Img = cv2.imread(self.ImageDir + "/" + Img_name)
            Img = Img[..., :: -1]
            Ann = cv2.imread(self.AnnotationDir + "/" + Ann_name,0)
            if (Img.ndim==2):
                  Img=np.expand_dims(Img,3)
                  Img = np.concatenate([Img, Img, Img], axis=2)
            Img = Img[:, :, 0:3]

            [NumCCmp, CCmpMask, CCompBB, CCmpCntr] = cv2.connectedComponentsWithStats((Ann == ClassNum).astype(np.uint8)) # apply connected component
            SegNum = np.random.randint(NumCCmp-1)+1
            Mask=(CCmpMask==SegNum).astype(np.uint8)
            bbox=CCompBB[SegNum][:4]

            [h,w,d]= Img.shape
            Rs=np.max((Hb/h,Wb/w))
            if Rs>1:
                h=int(np.max((h*Rs,Hb)))
                w=int(np.max((w*Rs,Wb)))
                Img=cv2.resize(Img,dsize=(w,h),interpolation = cv2.INTER_LINEAR)
                Mask=cv2.resize(Mask,dsize=(w,h),interpolation = cv2.INTER_NEAREST)
                bbox=(bbox.astype(np.float32)*Rs.astype(np.float32)).astype(np.int64)

            x1 = int(np.floor(bbox[0]))
            Wbox = int(np.floor(bbox[2]))
            y1 = int(np.floor(bbox[1]))
            Hbox = int(np.floor(bbox[3]))
            if Wb>Wbox:
                Xmax=np.min((w-Wb,x1))
                Xmin=np.max((0,x1-(Wb-Wbox)))
            else:
                Xmin=x1
                Xmax=np.min((w-Wb, x1+(Wbox-Wb)))

            if Hb>Hbox:
                Ymax=np.min((h-Hb,y1))
                Ymin=np.max((0,y1-(Hb-Hbox)))
            else:
                Ymin=y1
                Ymax=np.min((h-Hb, y1+(Hbox-Hb)))


            if not (Xmin>=Xmax or Ymin>=Ymax or Xmin<0 or Ymin<0 or Xmax>Img.shape[1] or Ymax>Img.shape[0]):
                        x0=np.random.randint(low=Xmin,high=Xmax+1)
                        y0=np.random.randint(low=Ymin,high=Ymax+1)
                        Img=Img[y0:y0+Hb,x0:x0+Wb,:]
                        Mask=Mask[y0:y0+Hb,x0:x0+Wb]
            Img=cv2.resize(Img,(Wb,Hb),interpolation = cv2.INTER_LINEAR)
            Mask=cv2.resize(Mask,(Wb,Hb),interpolation = cv2.INTER_NEAREST)


            if random.random() < 0.5:
                   Img = np.fliplr(Img)
                   Mask = np.fliplr(Mask)

            BImgs[i] = Img
            BSegmentMask[i,:,:] = Mask
            BLabels[i] = int(ClassNum)
            BLabelsOneHot[i,ClassNum] = 1

        return BImgs,BSegmentMask,BLabels, BLabelsOneHot

    def ReadNextImageClean(self,MaxBatchSize=40,MaxPixels=1500000):

        if self.ImageN>=len(self.FileList):
            print("No More files to read")
            return
        Img_name=self.FileList[self.ImageN].replace(self.AnnotationFileType,self.ImageFileType)
        Ann_name=self.FileList[self.ImageN]
        self.ImageN+=1
        Img = cv2.imread(self.ImageDir + "/" + Img_name)
        Img = Img[...,:: -1]
        Ann = cv2.imread(self.AnnotationDir + "/" + Ann_name, 0)
        if (Img.ndim == 2):
            Img = np.expand_dims(Img, 3)
            Img = np.concatenate([Img, Img, Img], axis=2)
        Img = Img[:, :, 0:3]

        Hb, Wb = Ann.shape
        Rt=MaxPixels/(Hb*Wb)
        if Rt<1:
            Hb*=Rt
            Wb*=Rt
            Img = cv2.resize(Img, (int(Wb),int(Hb)), interpolation=cv2.INTER_LINEAR)
            Ann = cv2.resize(Ann, (int(Wb),int(Hb)), interpolation=cv2.INTER_NEAREST)



        Hb,Wb=Ann.shape
        BImgs = np.zeros((MaxBatchSize, Hb, Wb, 3))
        BSegmentMask = np.zeros((MaxBatchSize, Hb, Wb))
        BLabels = np.zeros((MaxBatchSize), dtype=np.int)
        BLabelsOneHot = np.zeros((MaxBatchSize, self.NumClass + 1), dtype=np.float32)
        i=0
        NumClass=np.max(Ann)

        for ClassNum in range(1,NumClass+1):
            if ClassNum in Ann:
              [NumCCmp, CCmpMask, CCompBB, CCmpCntr] = cv2.connectedComponentsWithStats((Ann == ClassNum).astype(np.uint8))  # apply connected component
              for SegNum in range(1,NumCCmp):
                 Mask = (CCmpMask == SegNum).astype(np.uint8)
                 BImgs[i] = Img
                 BSegmentMask[i, :, :] = Mask
                 BLabels[i] = int(ClassNum)
                 BLabelsOneHot[i, ClassNum] = 1
                 i+=1

        BImgs = BImgs[:i]
        BSegmentMask = BSegmentMask[:i]
        BLabels = BLabels[:i]
        BLabelsOneHot = BLabelsOneHot[:i]
        return BImgs,BSegmentMask,BLabels, BLabelsOneHot

