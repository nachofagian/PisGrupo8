//
//  DataPregunta.h
//  PlainStockiOS
//
//  Created by Bruno Rodao on 10/3/15.
//  Copyright (c) 2015 FING. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DataAnswer.h"

@interface DataQuestion : NSObject

@property (nonatomic) NSInteger questionId;
@property (strong, nonatomic) NSString *textEng;
@property (strong, nonatomic) NSString *textSpa;
@property (strong, nonatomic) NSArray *answers;
@property (nonatomic) NSInteger selectedAnswer;

@end