import {Button, Checkbox, Form, Input, message, Modal} from "antd";
import {useState} from "react";
import {token} from "./UserApi";
import {createAsyncThunk} from "@reduxjs/toolkit";
import {useAppDispatch} from "../../app/hooks";

function LoginForm() {
    const dispatch = useAppDispatch();

    const asyncLogin = createAsyncThunk(
        'user/login',
        (user: {
            username: string,
            password: string
        }) => {
            return token(user).then(value => {
                const {status, jwt} = value.data
                return jwt
            }).catch(reason => {
                console.log(reason)
                message.error("Fail to login").then(r => {});
            })
        }
    )

    const onFinish = (values: any) => {
        const user = {
            username: values.username,
            password: values.password
        }
        dispatch(asyncLogin(user))
        // token(user).then(value => {
        //     console.log(user.username + " Login success.")
        // }).catch(reason => {
        //     console.log(reason)
        //     console.log(user.username + " login failed.")
        // })
    }

    return (
        <Form
            name={'login-form'}
            labelCol={{span: 8}}
            wrapperCol={{ span: 16 }}
            initialValues={{ remember: true }}
            autoComplete={'off'}
            onFinish={onFinish}
        >
            <Form.Item
                label={'Username'}
                name={'username'}
                rules={[{ required: true, message: 'Please input your username!' }]}
            >
                <Input/>
            </Form.Item>
            <Form.Item
                label={'Password'}
                name={'password'}
                rules={[{ required: true, message: 'Please input your password!' }]}
            >
                <Input.Password/>
            </Form.Item>
            <Form.Item name="remember" valuePropName="checked" wrapperCol={{ offset: 8, span: 16 }}>
                <Checkbox>Remember me</Checkbox>
            </Form.Item>

            <Form.Item wrapperCol={{ offset: 8, span: 16 }}>
                <Button type="primary" htmlType="submit">
                    Login
                </Button>
            </Form.Item>
        </Form>
    )
}

export function Login() {
    const[isOn, setIsOn] = useState(true)

    return (
        <div>
            <Modal
                title={'Login'}
                open={isOn}
                closable={false}
                centered={true}
                footer={null}
            >
                <LoginForm/>
            </Modal>

        </div>
    )
}