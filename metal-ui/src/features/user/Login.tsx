import {Button, Checkbox, Form, Input, message, Modal, Spin, Result, Alert} from "antd";
import {useEffect, useState} from "react";
import {authenticate, UserBasicCredentials} from "./UserApi";
import {useAppDispatch, useAppSelector} from "../../app/hooks";
import {auth, tokenSelector} from "./userSlice";
import {useSelector} from "react-redux";

enum State {
    idle,
    pending,
    success,
    fail
}

function LoginForm() {
    const dispatch = useAppDispatch()
    const [state, setState] = useState(State.idle)
    const [token, setToken] = useState<string|null>(null)

    const onFinish = (values: any) => {
        const user: UserBasicCredentials = {
            username: values.username,
            password: values.password
        }

        authenticate(user).then((_token: string) => {
            setState(State.success)
            setToken(_token)
        }, reason => {
            setState(State.fail)
        })
    }

    const result = state === State.fail ? <Alert message={'Fail to login'} type={'error'}/> : <></>
    useEffect(() => {
        dispatch(auth(token))
    }, [token, state])

    return (
        <Spin spinning={state == State.pending}>
            {result}
            <Form
                name={'login-form'}
                labelCol={{span: 8}}
                wrapperCol={{span: 16}}
                initialValues={{remember: true}}
                autoComplete={'off'}
                onFinish={onFinish}
            >
                <Form.Item
                    label={'Username'}
                    name={'username'}
                    rules={[{required: true, message: 'Please input your username!'}]}
                >
                    <Input/>
                </Form.Item>
                <Form.Item
                    label={'Password'}
                    name={'password'}
                    rules={[{required: true, message: 'Please input your password!'}]}
                >
                    <Input.Password/>
                </Form.Item>
                <Form.Item name="remember" valuePropName="checked" wrapperCol={{offset: 8, span: 16}}>
                    <Checkbox>Remember me</Checkbox>
                </Form.Item>

                <Form.Item wrapperCol={{offset: 8, span: 16}}>
                    <Button type="primary" htmlType="submit">
                        Login
                    </Button>
                </Form.Item>
            </Form>
        </Spin>
    )
}


export function Login() {
    const token: string | null = useAppSelector(state => {return tokenSelector(state)})
    const isOn = token == null

    if (!isOn) {
        return (<></>)
    }

    return (
        <Modal
            title={'Login'}
            open={isOn}
            closable={false}
            centered={true}
            footer={null}
        >
            <LoginForm/>
        </Modal>
    )
}