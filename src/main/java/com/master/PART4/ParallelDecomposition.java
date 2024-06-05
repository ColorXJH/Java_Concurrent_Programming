package com.master.PART4;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 并行分解
 * @date 2024-06-05 16:19
 */
public class ParallelDecomposition {
    //着重三个基于任务的设计系列：分支/合并fork/join  计算树computation tree 关卡barrier

    //分支/合并 分而治之计数的并行版本，伪代码如下
    /**
     * class Solver{
     *     Result solve(Param problem){
     *         if(problem.size<basicSize)
     *           return directlySolve(problem)
     *         else{
     *             Result l,r;
     *             in-parallel{
     *                 l=solve(leftHalf(problem));
     *                 r=solve(rightHalf(problem));
     *             }
     *             return combine(l,r);
     *         }
     *     }
     * }
     */
}
